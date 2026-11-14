package com.example.bookstore.author;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.NotFoundException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

	private final AuthorRepository authorRepository;

	public AuthorController(AuthorRepository authorRepository) {
		this.authorRepository = authorRepository;
	}

	public record RenameRequest(@NotBlank @Size(max = 100) String name) {}

	@GetMapping
	public List<Author> list(@RequestParam(required = false) String keyword) {
		return authorRepository.findAll(keyword);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Author create(@Valid @RequestBody AuthorRequest request) {
		return authorRepository.create(request);
	}

	@PutMapping("/{id}")
	public Author update(@PathVariable long id, @Valid @RequestBody AuthorRequest request) {
		return authorRepository.update(id, request)
			.orElseThrow(() -> notFound(id));
	}

	@PatchMapping("/{id}/name")
	public Author rename(@PathVariable long id, @Valid @RequestBody RenameRequest request) {
		return authorRepository.rename(id, request.name())
			.orElseThrow(() -> notFound(id));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		if (!authorRepository.deleteById(id)) {
			throw notFound(id);
		}
	}

	private static NotFoundException notFound(long id) {
		return new NotFoundException("저자를 찾을 수 없습니다: id=" + id);
	}
}
