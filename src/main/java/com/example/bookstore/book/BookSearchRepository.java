package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.*;
import static org.jooq.impl.DSL.*;

import java.math.BigDecimal;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Records;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.bookstore.common.PageResponse;
import com.example.bookstore.jooq.Tables;

@Repository
public class BookSearchRepository {

	private final DSLContext dsl;

	public BookSearchRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/** 오프셋 페이지네이션: 목록 쿼리 + 전체 건수 쿼리 */
	public PageResponse<BookSummary> search(BookSearchCondition condition, BookSort sort, int page, int size) {
		var where = toCondition(condition);

		var content = baseQuery()
			.where(where)
			.orderBy(sort.orderFields())
			.limit(size)
			.offset((long) page * size)
			.fetch(mapping(BookSummary::new));

		long total = dsl.fetchCount(BOOK, where);
		return PageResponse.of(content, page, size, total);
	}

	/** 키셋 페이지네이션: (price, id)가 커서보다 큰 행만 읽는다. */
	public BookScrollResponse scrollByPrice(BookSearchCondition condition, BigDecimal afterPrice, Long afterId, int size) {
		var where = toCondition(condition);
		if (afterPrice != null && afterId != null) {
			// WHERE (book.price, book.id) > (?, ?) - SQL 표준 value 비교
			where = where.and(row(BOOK.PRICE, BOOK.ID).gt(afterPrice, afterId));
		}

		// 다음 페이지 존재 여부를 알기 위해 한 건 더 읽는다.
		var rows = baseQuery()
			.where(where)
			.orderBy(BOOK.PRICE.asc(), BOOK.ID.asc())
			.limit(size + 1)
			.fetch(mapping(BookSummary::new));

		var hasNext = rows.size() > size;
		var content = hasNext ? rows.subList(0, size) : rows;
		if (!hasNext || content.isEmpty()) {
			return new BookScrollResponse(content, false, null, null);
		}
		var last = content.getLast();
		return new BookScrollResponse(content, true, last.price(), last.id());
	}

	/**
	 * 목록·스크롤이 공유하는 SELECT ... FROM ... JOIN 부분
	 */
	private SelectOnConditionStep<Record6<Long, String, String, BigDecimal, Integer, String>> baseQuery() {
		return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE, BOOK.STOCK, CATEGORY.NAME)
			.from(BOOK)
			.join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID));
	}

	/**
	 * 검색 조건 -> jOOQ Condition. 값이 있는 조건만 AND로 쌓는다.
	 */
	private Condition toCondition(BookSearchCondition c) {
		var where = noCondition();

		if (hasText(c.keyword())) {
			where = where.and(BOOK.TITLE.containsIgnoreCase(c.keyword()));
		}
		if (c.categoryId() != null) {
			where = where.and(BOOK.CATEGORY_ID.eq(c.categoryId()));
		}
		if (c.minPrice() != null) {
			where = where.and(BOOK.PRICE.ge(c.minPrice()));
		}
		if (c.maxPrice() != null) {
			where = where.and(BOOK.PRICE.le(c.maxPrice()));
		}
		// tags @> ARRAY[?]::text[] - GIN 인덱스를 탈 수 있는 형태
		if (hasText(c.tag())) {
			where = where.and(condition("{0} @> {1}",
				BOOK.TAGS, val(new String[] {c.tag()}, BOOK.TAGS)));
		}
		if (hasText(c.language())) {
			// attributes =>> 'language' = ? - JSONB 연산자는 Plain SQL 템플릿으로 표현
			where = where.and(condition("{0} ->> 'language' = {1}",
				BOOK.ATTRIBUTES, val(c.language())));
		}
		if (Boolean.TRUE.equals(c.inStock())) {
			where = where.and(BOOK.STOCK.gt(0));
		}

		return where;
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}
}
