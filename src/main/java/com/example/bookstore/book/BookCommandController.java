package com.example.bookstore.book;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookCommandController {

	private final BookCommandService bookCommandService;
	private final BookDetailRepository bookDetailRepository;

	public BookCommandController(BookCommandService bookCommandService,
		BookDetailRepository bookDetailRepository) {
		this.bookCommandService = bookCommandService;
		this.bookDetailRepository = bookDetailRepository;
	}

	@PostMapping
	public ResponseEntity<BookDetail> create(
		@Valid @RequestBody BookCreateRequest request) {
		long id = bookCommandService.create(request);
		var detail = bookDetailRepository.findById(id).orElseThrow();
		return ResponseEntity.created(URI.create("/api/books/" + id + "/detail"))
			.body(detail);
	}

	@PatchMapping("/{id}/price")
	public BookDetail changePrice(@PathVariable long id,
		@Valid @RequestBody PriceChangeRequest request) {
		bookCommandService.changePrice(id, request.price(), request.version());
		return bookDetailRepository.findById(id).orElseThrow();
	}
}
