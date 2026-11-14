package com.example.bookstore.common;

/** 현재 상태와 충돌하는 요청일 떄: 재고 부족, 동시 수정, 잘못된 상태 전이 (HTTP 409) */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
