package com.example.bookstore.stats;

import java.math.BigDecimal;

public record BestSeller(
	String categoryName,
	Long bookId,
	String title,
	BigDecimal soldQuantity,
	Integer rank
) {
}
