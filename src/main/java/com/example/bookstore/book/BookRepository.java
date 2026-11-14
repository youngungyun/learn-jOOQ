package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.*;
import static org.jooq.impl.DSL.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

	private final DSLContext dsl;

	public BookRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/** 방법 1: 명시적 INNER JOIN. SQL과 가장 비슷하게 읽힌다. */
	public Optional<BookSummary> findSummaryById(long id) {
		return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE,
			BOOK.STOCK, CATEGORY.NAME)
			.from(BOOK)
			.join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID))
			.where(BOOK.ID.eq(id))
			.fetchOptional(mapping(BookSummary::new));
	}

	/** 방법 2: 암시적 path 조인. FROM 절에 CATEGORY가 없어도 jOOQ가 조인을 만든다. */
	public List<BookSummary> findByCategoryName(String categoryName) {
		return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE,
			BOOK.STOCK, BOOK.category().NAME)
			.from(BOOK)
			.where(BOOK.category().NAME.eq(categoryName))
			.orderBy(BOOK.TITLE)
			.fetch(mapping(BookSummary::new));
	}

	/**
	 * 방법 3: LEFT JOIN + GROUP BY + array_agg.
	 * 저자가 없는 도서도 나오도록 LEFT JOIN하고, FILTER로 NULL을 배열에서 뺸다.
	 */
	public List<BookWithAuthors> findAllWithAuthorNames() {
		Field<List<String>> authorNames = coalesce(
			arrayAgg(AUTHOR.NAME)
				.orderBy(AUTHOR.NAME)
				.filterWhere(AUTHOR.ID.isNotNull()),
			val(new String[0]))
			// 애드혹 변환기: 이 쿼리에서만 String[]를 List<String>으로 바꾼다.
			.convertFrom(Arrays::asList);

		return dsl.select(BOOK.ID, BOOK.TITLE, CATEGORY.NAME, authorNames)
			.from(BOOK)
			.join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID))
			.leftJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
			.leftJoin(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
			.groupBy(BOOK.ID, BOOK.TITLE, CATEGORY.NAME)
			.orderBy(BOOK.ID)
			.limit(100)
			.fetch(mapping(BookWithAuthors::new));
	}
}
