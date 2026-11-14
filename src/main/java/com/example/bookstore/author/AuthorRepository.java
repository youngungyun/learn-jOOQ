package com.example.bookstore.author;

import static com.example.bookstore.jooq.Tables.AUTHOR;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import com.example.bookstore.jooq.tables.records.AuthorRecord;

@Repository
public class AuthorRepository {

	private final DSLContext dsl;

	public AuthorRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/** 목록 조회. keyword가 있으면 이름 부분 일치(대소문자 무시)로 거른다. */
	public List<Author> findAll(String keyword) {
		var condition = (keyword == null || keyword.isBlank())
			? DSL.noCondition()
			: AUTHOR.NAME.containsIgnoreCase(keyword);

		return dsl.selectFrom(AUTHOR)
			.where(condition)
			.orderBy(AUTHOR.ID)
			.fetch(AuthorRepository::toAuthor);
	}

	// 연습 과제 4-2.
	public List<Author> findAll(int fromYear, int toYear) {
		return dsl.selectFrom(AUTHOR)
			.where(AUTHOR.BIRTH_YEAR.between(fromYear, toYear))
			.fetch(AuthorRepository::toAuthor);
	}

	public Optional<Author> findById(long id) {
		return dsl.selectFrom(AUTHOR)
			.where(AUTHOR.ID.eq(id))
			.fetchOptional(AuthorRepository::toAuthor);
	}

	/** INSERT ... RETURNING * : 생성된 id와 DB 기본값(created_at)을 한 번에 돌려받는다. */
	public Author create(AuthorRequest request) {
		var inserted = dsl.insertInto(AUTHOR)
			.set(AUTHOR.NAME, request.name())
			.set(AUTHOR.BIRTH_YEAR, request.birthYear())
			.returning()
			.fetchOne();

		return toAuthor(inserted);
	}

	// 연습 과제 4-3.
	public Author createWithRecord(AuthorRequest request) {
		var author = dsl.newRecord(AUTHOR);
		author.setName(request.name());
		author.setBirthYear(request.birthYear());
		author.store();
		author.refresh();
		return toAuthor(author);
	}

	/** 방법 1: DSL로 UPDATE ... RETURNING. 한 번의 왕복으로 수정과 조회를 끝낸다. */
	public Optional<Author> update(long id, AuthorRequest request) {
		return dsl.update(AUTHOR)
			.set(AUTHOR.NAME, request.name())
			.set(AUTHOR.BIRTH_YEAR, request.birthYear())
			.where(AUTHOR.ID.eq(id))
			.returning()
			.fetchOptional()
			.map(AuthorRepository::toAuthor);
	}

	/**
	 * 방법 2: 액티브 레코드. 레코드를 읽고, 값을 바꾸고, store()를 호출한다.
	 * jOOQ는 변경된 컬럼(name)만 UPDATE 문에 넣는다. (SELECT 1번 + UPDATE 1번)
	 */
	public Optional<Author> rename(long id, @NonNull String newName) {
		var record = dsl.fetchOne(AUTHOR, AUTHOR.ID.eq(id));
		if (record == null) {
			return Optional.empty();
		}
		record.setName(newName);
		record.store();
		return Optional.of(toAuthor(record));
	}

	public boolean deleteById(long id) {
		return dsl.deleteFrom(AUTHOR)
			.where(AUTHOR.ID.eq(id))
			.execute() > 0;
	}

	public boolean existsById(long id) {
		return dsl.fetchExists(AUTHOR, AUTHOR.ID.eq(id));
	}

	private static Author toAuthor(AuthorRecord r) {
		return new Author(r.getId(), r.getName(), r.getBirthYear(), r.getCreatedAt());
	}
}