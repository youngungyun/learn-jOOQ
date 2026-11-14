package com.example.bookstore.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.example.bookstore.jooq.enums.OrderStatus;

public record OrderSummary(
	Long id,
	OrderStatus status,
	BigDecimal totalAmount,
	OffsetDateTime orderedAt,
	Integer itemCount
) {
}
