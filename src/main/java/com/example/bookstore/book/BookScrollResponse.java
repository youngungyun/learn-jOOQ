package com.example.bookstore.book;

import java.math.BigDecimal;
import java.util.List;

/**
 * 키셋(커서) 기반 페이지네이션 응답.
 */
public record BookScrollResponse(
	List<BookSummary> content,
	boolean hasNext,
	BigDecimal nextAfterPrice,
	Long nextAfterId
) {
}
