package com.example.bookstore.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.example.bookstore.jooq.enums.OrderStatus;

public record OrderDetail(
	Long id,
	Long memberId,
	String memberName,
	OrderStatus status,
	BigDecimal totalAmount,
	OffsetDateTime orderedAt,
	List<OrderLine> items
) {
}
