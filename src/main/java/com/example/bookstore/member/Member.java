package com.example.bookstore.member;

import java.time.OffsetDateTime;

public record Member(
	Long id,
	String email,
	String name,
	OffsetDateTime createdAt
) {
}
