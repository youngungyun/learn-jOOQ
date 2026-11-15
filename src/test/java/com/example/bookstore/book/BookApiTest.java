package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.AUTHOR;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookstore.TestcontainersConfiguration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BookApiTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	DSLContext dsl;

	@Test
	@DisplayName("태그로 검색하면 배열 포함 조건(@>)으로 걸러진다")
	void searchByTag() throws Exception {
		mockMvc.perform(get("/api/books/search").param("tag", "jooq"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].title").value("jOOQ로 배우는 타입 안전 SQL"));
	}

	@Test
	@DisplayName("도서를 등록하면 MULTISET 상세가 반환되고, 같은 ISBN 은 409")
	void createBook() throws Exception {
		long categoryId = dsl.select(CATEGORY.ID).from(CATEGORY)
			.where(CATEGORY.NAME.eq("프로그래밍")).fetchSingle().value1();
		long authorId = dsl.select(AUTHOR.ID).from(AUTHOR)
			.where(AUTHOR.NAME.eq("김지훈")).fetchSingle().value1();

		String body = """
                {
                  "categoryId": %d,
                  "title": "API 테스트 도서",
                  "isbn": "9799000000001",
                  "price": 12000,
                  "stock": 3,
                  "tags": ["testing"],
                  "attributes": {"language": "ko"},
                  "authorIds": [%d]
                }
                """.formatted(categoryId, authorId);

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.authors[0].name").value("김지훈"))
			.andExpect(jsonPath("$.category.name").value("프로그래밍"))
			.andExpect(jsonPath("$.attributes.language").value("ko"))
			.andExpect(jsonPath("$.attributes.pages").doesNotExist());

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("잘못된 ISBN 은 400 과 필드별 오류를 돌려준다")
	void validationError() throws Exception {
		String body = """
                {"categoryId": 1, "title": "x", "isbn": "123", "price": 1000, "stock": 1, "authorIds": [1]}
                """;

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.isbn").exists());
	}
}