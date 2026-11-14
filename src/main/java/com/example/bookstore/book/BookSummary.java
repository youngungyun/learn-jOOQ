package com.example.bookstore.book;

import java.math.BigDecimal;

/** 목록·검색용 도서 요약. 5장(조회)과 6장(검색)에서 공유한다. */
public record BookSummary(
	Long id,
	String title,
	String isbn,
	BigDecimal price,
	Integer stock,
	String categoryName
) {
}
