package com.example.bookstore.book;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** version: 클라이언트가 도서 상세(7장)에서 받은 값을 그대로 돌려보낸다. */
public record PriceChangeRequest (
	@NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal price,
	@NotNull Integer version
) {

}
