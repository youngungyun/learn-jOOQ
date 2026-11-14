package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.select;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import com.example.bookstore.jooq.Tables;

@Repository
public class BookDetailRepository {

	private final DSLContext dsl;

	public BookDetailRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	public Optional<BookDetail> findById(long id) {
		return dsl.select(
			BOOK.ID,
			BOOK.TITLE,
			BOOK.ISBN,
			BOOK.PRICE,
			BOOK.STOCK,
			BOOK.PUBLISHED_ON,
			BOOK.VERSION,
			BOOK.TAGS.convertFrom(Arrays::asList),
			BOOK.ATTRIBUTES,
			row(BOOK.category().ID,
				BOOK.category().NAME)
				.mapping(CategoryRef::new),
			authorsOfBook())
			.from(BOOK)
			.where(BOOK.ID.eq(id))
			.fetchOptional(mapping(BookDetail::new));
	}

	private static Field<List<AuthorRef>> authorsOfBook() {
		return multiset(
			select(AUTHOR.ID, AUTHOR.NAME)
				.from(BOOK_AUTHOR)
				.join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
				.where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
				.orderBy(AUTHOR.NAME))
			.as("authors")
			.convertFrom(result -> result.map(
				mapping(AuthorRef::new))
			);
	}
}
