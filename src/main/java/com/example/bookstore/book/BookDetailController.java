package com.example.bookstore.book;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.NotFoundException;

@RestController
@RequestMapping("/api/books")
public class BookDetailController {

	private final BookDetailRepository bookDetailRepository;

	public BookDetailController(BookDetailRepository bookDetailRepository) {
		this.bookDetailRepository = bookDetailRepository;
	}

	@GetMapping("/{id}/detail")
	public BookDetail detail(@PathVariable long id) {
		return bookDetailRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다: id=" + id));
	}
}
