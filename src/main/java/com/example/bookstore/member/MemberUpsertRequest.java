package com.example.bookstore.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpsertRequest(
	@NotBlank @Email @Size(max = 255) String email,
	@NotBlank @Size(max = 100) String name
) {
}
