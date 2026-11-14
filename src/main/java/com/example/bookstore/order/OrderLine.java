package com.example.bookstore.order;

import java.math.BigDecimal;

public record OrderLine(
	Long bookId,
	String title,
	Integer quantity,
	BigDecimal unitPrice,
	BigDecimal lineAmount
) {
}
