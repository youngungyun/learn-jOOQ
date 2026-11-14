package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.*;

import java.util.List;

import org.jooq.SortField;

import com.example.bookstore.jooq.Tables;

/**
 * 해용된 정렬 방식만 enum으로 제공한다.
 * 사용자 입력 문자열을 컬럼 이름으로 직접 쓰지 않으므로 ORDER BY 인젝션이 불가능하다.
 * 모든 정렬의 마지막에 id를 두어 순서를 완전히 결정적으로 만든다.
 */
public enum BookSort {
	NEWEST,
	PRICE_ASC,
	PRICE_DESC,
	TITLE;

	public List<SortField<?>> orderFields() {
		return switch (this) {
			case NEWEST -> List.of(BOOK.PUBLISHED_ON.desc().nullsLast(), BOOK.ID.desc());
			case PRICE_ASC -> List.of(BOOK.PRICE.asc(), BOOK.ID.asc());
			case PRICE_DESC -> List.of(BOOK.PRICE.desc(), BOOK.ID.desc());
			case TITLE -> List.of(BOOK.TITLE.asc(), BOOK.ID.asc());
		};
	}
}
