package com.example.bookstore.book;

/** 도서 상세 안에 포함되는 카테고리 참조 */
public record CategoryRef(
	Long id,
	String name
) {
}
