package com.example.bookstore.book;

import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.NotFoundException;

@RestController
public class BookAdminController {

	private final BookAdminRepository bookAdminRepository;

	public BookAdminController(BookAdminRepository bookAdminRepository) {
		this.bookAdminRepository = bookAdminRepository;
	}

	@PostMapping("/api/books/{id}/tags/{tag}")
	public List<String> addTag(@PathVariable long id, @PathVariable String tag) {
		return bookAdminRepository.addTag(id, tag.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> notFound(id));
	}

	@DeleteMapping("/api/books/{id}/tags/{tag}")
	public List<String> removeTag(@PathVariable long id, @PathVariable String tag) {
		return bookAdminRepository.removeTag(id, tag.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> notFound(id));
	}

	@PatchMapping("/api/books/{id}/attributes")
	public BookAttributes mergeAttributes(@PathVariable long id, @RequestBody BookAttributes patch) {
		return bookAdminRepository.mergeAttributes(id, patch)
			.orElseThrow(() -> notFound(id));
	}

	@GetMapping("/api/tags/stats")
	public List<TagCount> tagStats() {
		return bookAdminRepository.tagStats();
	}

	private static NotFoundException notFound(long id) {
		return new NotFoundException("도서를 찾을 수 없습니다: id=" + id);
	}
}
