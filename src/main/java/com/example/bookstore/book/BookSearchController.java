package com.example.bookstore.book;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.PageResponse;

@RestController
@RequestMapping("/api/books")
public class BookSearchController {

	private static final int MAX_PAGE_SIZE = 100;

	private final BookSearchRepository bookSearchRepository;

	public BookSearchController(BookSearchRepository bookSearchRepository) {
		this.bookSearchRepository = bookSearchRepository;
	}

	@GetMapping("/search")
	public PageResponse<BookSummary> search(@ModelAttribute BookSearchCondition condition,
		@RequestParam(defaultValue = "NEWEST") BookSort sort,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		return bookSearchRepository.search(
			condition, sort, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE)
		);
	}

	@GetMapping("/scroll")
	public BookScrollResponse scroll(@ModelAttribute BookSearchCondition condition,
		@RequestParam(required = false) BigDecimal afterPrice,
		@RequestParam(required = false) Long afterId,
		@RequestParam(defaultValue = "20") int size) {
		return bookSearchRepository.scrollByPrice(
			condition, afterPrice, afterId, Math.clamp(size, 1, MAX_PAGE_SIZE)
		);
	}
}
