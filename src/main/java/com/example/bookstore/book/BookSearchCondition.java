package com.example.bookstore.book;

import java.math.BigDecimal;

/**
 * 도서 검색 조건. 모든 필드는 선택 사항이며, null이면 해당 조건을 적용하지 않는다.
 * 쿼리 파라미터가 생성자에 파인딩된다.
 */
public record BookSearchCondition(
	String keyword,
	Long categoryId,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	String tag,
	String language,
	Boolean inStock
) {
}
