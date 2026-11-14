package com.example.bookstore.book;

/** 도서 상세 안에 포함되는 저자 참조 */
public record AuthorRef(
	Long id,
	String name
) {
}
