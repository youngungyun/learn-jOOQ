package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.BOOK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.row;

import java.math.BigDecimal;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Spring 컨텍스트와 DB 없이 jOOQ 가 만드는 SQL 만 검사한다. (수 밀리초) */
class QueryRenderingTest {

	private final DSLContext dsl = DSL.using(SQLDialect.POSTGRES);

	@Test
	@DisplayName("키셋 조건은 row value 비교로 렌더링된다")
	void keysetCondition() {
		String sql = dsl.renderInlined(
			dsl.select(BOOK.ID)
				.from(BOOK)
				.where(row(BOOK.PRICE, BOOK.ID).gt(new BigDecimal("22000"), 4L)));

		assertThat(sql).contains("> (").contains("22000");
	}

	@Test
	@DisplayName("조건이 없으면 WHERE 절이 렌더링되지 않는다")
	void noCondition() {
		String sql = dsl.render(dsl.select(BOOK.ID).from(BOOK).where(DSL.noCondition()));

		assertThat(sql).doesNotContain("where");
	}
}