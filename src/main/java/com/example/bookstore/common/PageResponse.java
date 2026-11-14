package com.example.bookstore.common;

import java.util.List;

/** 오프셋 페이지네이션 응답 (page는 0부터 시작) */
public record PageResponse<T> (
	List<T> content,
	int page,
	int size,
	long totalElements,
	int totalPages
) {

	public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
		var totalPages = (int) Math.ceil((double) totalElements / size);
		return new PageResponse<>(content, page, size, totalElements, totalPages);
	}
}
