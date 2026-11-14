package com.example.bookstore.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * book.attributes(JSONB) 컬럼에 대응하는 값 객체.
 * null인 속성은 JSON에서 생략한다 (9자의 JSON 병합 연산에서 중요).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookAttributes(
	String language,
	Integer pages,
	String format
) {
	public static BookAttributes empty() {
		return new BookAttributes(null, null, null);
	}
}