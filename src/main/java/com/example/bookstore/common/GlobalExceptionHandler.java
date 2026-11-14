package com.example.bookstore.common;

import static org.springframework.http.HttpStatus.*;

import java.util.LinkedHashMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 예외를 RFC 9457 ProblemDetail 형식으로 응답한다.
 * jOOQ가 던진 SQL 에러는 Spring Boot의 jOOQ 자동 설정이 Spring DataAccessException으로
 * 변환해 주므로(11장), 여기서는 Spring 예외 타입만 다루면 된다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail handleNotFound(NotFoundException e) {
		return problem(NOT_FOUND, "리소스를 찾을 수 없음", e.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflict(ConflictException e) {
	return problem(CONFLICT, "요청이 현재 상태와 충돌함", e.getMessage());
	}

	/** UNIQUE 제약 위반 (예, 중복 ISBN). DataIntegrityViolationExceptionㅢ 하위 타입이라 먼저 매칭된다. */
	@ExceptionHandler(DuplicateKeyException.class)
	public ProblemDetail handleDuplicateKey(DuplicateKeyException e) {
		return problem(CONFLICT, "중복된 데이터", "이미 존재하는 값입니다.");
	}

	/** FX·CHECK·NOT NULL 제약 위반 (예: 도서가 있는 저자 삭제) */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleIntegrity(DataIntegrityViolationException e) {
		return problem(CONFLICT, "데이터 무결성 위반",
			"다른 데이터가 참조하고 있거나 제약 조건을 위반했습니다.");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
		var pd = problem(BAD_REQUEST, "입력값 검증 실패", "요청 본문에 잘못된 값이 있습니다.");
		var errors = new LinkedHashMap<>();
		for (var filedError : e.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(filedError.getField(), filedError.getDefaultMessage());
		}
		pd.setProperty("errors", errors);
		return pd;
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		var pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		return pd;
	}
}
