package com.example.bookstore.author;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookstore.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** 각 테스트는 트랜잭션 안에서 실행되고 끝나면 롤백된다. (jOOQ 도 Spring 트랜잭션을 따른다) */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthorRepositoryTest {

	@Autowired
	AuthorRepository authorRepository;

	@Test
	@DisplayName("저자를 만들면 id와 created_at 이 채워져 돌아온다")
	void create() {
		Author created = authorRepository.create(new AuthorRequest("테스트 저자", 1990));

		assertThat(created.id()).isNotNull();
		assertThat(created.createdAt()).isNotNull();
		assertThat(authorRepository.findById(created.id()))
			.get()
			.extracting(Author::name)
			.isEqualTo("테스트 저자");
	}

	@Test
	@DisplayName("키워드는 대소문자를 무시하고 부분 일치로 검색한다")
	void findByKeyword() {
		assertThat(authorRepository.findAll("EMILY"))
			.extracting(Author::name)
			.containsExactly("Emily Carter");
	}

	@Test
	@DisplayName("DSL UPDATE 와 액티브 레코드 store() 모두 값을 바꾼다")
	void updateAndRename() {
		Author created = authorRepository.create(new AuthorRequest("원래 이름", 1980));

		assertThat(authorRepository.update(created.id(), new AuthorRequest("수정 이름", 1981)))
			.get()
			.satisfies(a -> {
				assertThat(a.name()).isEqualTo("수정 이름");
				assertThat(a.birthYear()).isEqualTo(1981);
			});

		assertThat(authorRepository.rename(created.id(), "다시 수정"))
			.get()
			.extracting(Author::name)
			.isEqualTo("다시 수정");
	}

	@Test
	@DisplayName("없는 id 는 빈 Optional / false 를 돌려준다")
	void notFound() {
		assertThat(authorRepository.findById(-1L)).isEmpty();
		assertThat(authorRepository.update(-1L, new AuthorRequest("x", null))).isEmpty();
		assertThat(authorRepository.deleteById(-1L)).isFalse();
	}
}