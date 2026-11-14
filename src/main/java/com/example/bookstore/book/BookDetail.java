package com.example.bookstore.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 도세 상세. version은 8장의 가겨 변경(낙관적 락)에서 클라이언트가 돌려보내는 값이다.
 */
public record BookDetail(
	Long id,
	String title,
	String isbn,
	BigDecimal price,
	Integer stock,
	LocalDate publishedOn,
	Integer version,
	List<String> tags,
	BookAttributes attributes,
	CategoryRef category,
	List<AuthorRef> authors
) {
}
