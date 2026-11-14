package com.example.bookstore.author;

import java.time.OffsetDateTime;

/** API 응답용 저자 DTO. 생성 코드를 API에 직접 노출하지 않는다. */
public record Author(
	Long id,
	String name,
	Integer birthYear,
	OffsetDateTime createdAt
) {
}
