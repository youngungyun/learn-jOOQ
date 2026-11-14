package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.common.ConflictException;
import com.example.bookstore.common.NotFoundException;
import com.example.bookstore.jooq.Tables;

@Service
public class BookCommandService {

	private final DSLContext dsl;

	public BookCommandService(DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * 도서와 도서-저자 연결을 한 트랙잭션으로 저장한다.
	 * 중간에 예외가 나면(예: 중복 ISBN) 이미 실행된 INSERT도 모두 롤백된다.
	 */
	@Transactional
	public long create(BookCreateRequest request) {
		if (!dsl.fetchExists(CATEGORY, CATEGORY.ID.eq(request.categoryId()))) {
			throw new NotFoundException("카테고리를 찾을 수 없습니다: id=" + request.categoryId());
		}
		var authorIds = new LinkedHashSet<Long>(request.authorIds());
		int foundAuthors = dsl.fetchCount(AUTHOR, AUTHOR.ID.in(authorIds));
		if (foundAuthors != authorIds.size()) {
			throw new NotFoundException("존재하지 않는 저자가 포함되어 있습니다: " + authorIds);
		}

		// INSERT ... RETURNING id
		Long bookId = dsl.insertInto(BOOK)
			.set(BOOK.CATEGORY_ID, request.categoryId())
			.set(BOOK.TITLE, request.title())
			.set(BOOK.ISBN, request.isbn())
			.set(BOOK.PRICE, request.price())
			.set(BOOK.STOCK, request.stock())
			.set(BOOK.PUBLISHED_ON, request.publishedOn())
			.set(BOOK.TAGS, request.tags().toArray(String[]::new))
			.set(BOOK.ATTRIBUTES, request.attributes())
			.returningResult(BOOK.ID)
			.fetchOne()
			.value1();

		// 다중 행 INSERT
		var insert = dsl.insertInto(BOOK_AUTHOR, BOOK_AUTHOR.BOOK_ID,
			BOOK_AUTHOR.AUTHOR_ID);
		for (var authorId : authorIds) {
			insert = insert.values(bookId, authorId);
		}
		insert.execute();

		return bookId;
	}

	/**
	 * 낙관적 락: 클라이언트가 본 version과 DB의 version이 같을 때만 수정하고 version을 1 늘린다.
	 */
	@Transactional
	public void changePrice(long bookId, BigDecimal newPrice, int expectedVersion) {
		int updated = dsl.update(BOOK)
			.set(BOOK.PRICE, newPrice)
			.set(BOOK.VERSION, BOOK.VERSION.plus(1))
			.where(BOOK.ID.eq(bookId))
			.and(BOOK.VERSION.eq(expectedVersion))
			.execute();

		if (updated == 0) {
			if (!dsl.fetchExists(BOOK, BOOK.ID.eq(bookId))) {
				throw new NotFoundException("도서를 찾을 수 없습니다: id" + bookId);
			}
			throw new ConflictException("다른 사용자가 먼저 수정했습니다. 최신 정보를 다시 조회한 후 시도하세요.");
		}
	}
}
