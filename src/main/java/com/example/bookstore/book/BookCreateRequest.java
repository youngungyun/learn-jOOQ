package com.example.bookstore.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
	@NotNull Long categoryId,
	@NotBlank @Size(max = 200) String title,
	@NotBlank @Pattern(regexp = "\\d{13}", message = "ISBN은 숫자 13자리여야 합니다.") String isbn,
	@NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal price,
	@NotNull @PositiveOrZero Integer stock,
	LocalDate publishedOn,
	List<@NotBlank String> tags,
	BookAttributes attributes,
	@NotEmpty List<@NotNull Long> authorIds
	) {

	public BookCreateRequest {
		tags = (tags == null) ? List.of() : tags;
		attributes = (attributes == null) ? BookAttributes.empty() : attributes;
	}
}
