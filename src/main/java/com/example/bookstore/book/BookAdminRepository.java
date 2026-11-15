package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

@Repository
public class BookAdminRepository {

	private final DSLContext dsl;

	public BookAdminRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * 태그 추가. 이미 있으면 그대로 두고, 없으면 배열 끝에 붙인다.
	 * UPDATE book SET tags = CASE WHEN ? = ANY(tags) THEN tags
	 * ELSE array_append(tags, ?) END
	 * WHERE id = ? RETURNING tags
	 */
	public Optional<List<String>> addTag(long bookId, String tag) {
		var newTags = when(val(tag).eq(any(BOOK.TAGS)), BOOK.TAGS)
			.otherwise(arrayAppend(BOOK.TAGS, tag));

		return dsl.update(BOOK)
			.set(BOOK.TAGS, newTags)
			.where(BOOK.ID.eq(bookId))
			.returningResult(BOOK.TAGS)
			.fetchOptional()
			.map(r -> Arrays.asList(r.value1()));
	}

	public Optional<List<String>> removeTag(long bookId, String tag) {
		return dsl.update(BOOK)
			.set(BOOK.TAGS, arrayRemove(BOOK.TAGS, tag))
			.where(BOOK.ID.eq(bookId))
			.returningResult(BOOK.TAGS)
			.fetchOptional()
			.map(r -> Arrays.asList(r.value1()));
	}

	/**
	 * 태그별 도서 수. unnest로 배열을 행으로 펼친 뒤 GROUP BY한다.
	 * SELECT t.tag, count(*) FROM book, unnest(book.tags) AS t(tag) GROUP BY t.tag
	 */
	public List<TagCount> tagStats() {
		Table<?> tags = unnest(BOOK.TAGS).as("t", "tag");
		var tag = field(name("t", "tag"), String.class);

		return dsl.select(tag, count())
			.from(BOOK, tags)
			.groupBy(tag)
			.orderBy(count().desc(), tag.asc())
			.fetch(mapping(TagCount::new));
	}

	/**
	 * JSON 부분 병합: 전달된 속성만 덮어쓰고 나머지는 유지한다.
	 * UPDATE book SET attributes = attributes || ?::jsonb WHERE id = ?
	 * RETURNING attributes
	 */
	public Optional<BookAttributes> mergeAttributes(long bookId, BookAttributes patch) {
		var merged = field(
			"{0} || {1}",
			BOOK.ATTRIBUTES.getDataType(), // 결과 타입: Converter 포함 JSONB
			BOOK.ATTRIBUTES,
			val(patch, BOOK.ATTRIBUTES) // 바인드 값도 Converter로 JSONB 변환
		);

		return dsl.update(BOOK)
			.set(BOOK.ATTRIBUTES, merged)
			.where(BOOK.ID.eq(bookId))
			.returningResult(BOOK.ATTRIBUTES)
			.fetchOptional()
			.map(Record1::value1);
	}
}
