package com.example.bookstore.book;

import java.util.List;

public record BookWithAuthors(
	Long id,
	String title,
	String categoryName,
	List<String> authors
) {
}
