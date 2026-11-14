package com.example.bookstore.book;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.NotFoundException;

@RestController
@RequestMapping("/api/books")
public class BookController {

	private final BookRepository bookRepository;

	public BookController(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@GetMapping("/{id}")
	public BookSummary get(@PathVariable long id) {
		return bookRepository.findSummaryById(id)
			.orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다: id=" + id));
	}

	@GetMapping(params = "category")
	public List<BookSummary> listByCategory(@RequestParam String category) {
		return bookRepository.findByCategoryName(category);
	}

	@GetMapping("/with-authors")
	public List<BookWithAuthors> listWithAuthors() {
		return bookRepository.findAllWithAuthorNames();
	}
}
