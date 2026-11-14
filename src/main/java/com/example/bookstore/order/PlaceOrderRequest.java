package com.example.bookstore.order;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
	@NotNull Long memberId,
	@NotEmpty List<@Valid @NotNull Item> items
	) {

	public record Item(
		@NotNull Long bookId,
		@NotNull @Min(1) @Max(100) Integer quantity
	) {

	}
}
