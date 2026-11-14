package com.example.bookstore.author;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
	@NotBlank @Size(max = 100) String name,
	@Min(1000) @Max(2100) Integer birthYear
) {
}
