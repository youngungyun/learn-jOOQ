package com.example.bookstore.category;

public record CategoryNode(
	Long id,
	Long parentId,
	String name,
	Integer depth,
	String path
) {
}
