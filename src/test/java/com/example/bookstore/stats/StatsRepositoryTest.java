package com.example.bookstore.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookstore.TestcontainersConfiguration;
import com.example.bookstore.category.CategoryNode;
import com.example.bookstore.category.CategoryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StatsRepositoryTest {

	@Autowired
	StatsRepository statsRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Test
	@DisplayName("월별 매출은 PAID/SHIPPED 만 집계하고 누적·전월 값을 함께 계산한다")
	void monthlySales() {
		// 다른 테스트가 만든 최근 주문의 영향을 피하려고 시드 기간(2026-07 ~ 2026-10)만 본다
		List<MonthlySales> sales = statsRepository.monthlySales().stream()
			.filter(s -> !s.month().isAfter(LocalDate.of(2026, 10, 1)))
			.toList();

		assertThat(sales).extracting(MonthlySales::month).containsExactly(
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 1));

		MonthlySales august = sales.get(1);
		assertThat(august.orderCount()).isEqualTo(2);
		assertThat(august.revenue()).isEqualByComparingTo("117000");
		assertThat(august.cumulativeRevenue()).isEqualByComparingTo("217000");
		assertThat(august.previousMonthRevenue()).isEqualByComparingTo("100000");
		assertThat(sales.getFirst().previousMonthRevenue()).isNull();
	}

	@Test
	@DisplayName("카테고리별 1위: 프로그래밍은 jOOQ 책, 데이터베이스는 공동 1위 2권")
	void bestSellers() {
		List<BestSeller> top1 = statsRepository.bestSellersByCategory(1);

		assertThat(top1).filteredOn(b -> b.categoryName().equals("프로그래밍"))
			.extracting(BestSeller::title)
			.containsExactly("jOOQ로 배우는 타입 안전 SQL");
		assertThat(top1).filteredOn(b -> b.categoryName().equals("데이터베이스"))
			.hasSize(2)
			.allSatisfy(b -> assertThat(b.rank()).isEqualTo(1));
	}

	@Test
	@DisplayName("재귀 CTE는 깊이와 경로를 계산한다")
	void categoryTree() {
		List<CategoryNode> tree = categoryRepository.findTree();

		assertThat(tree).filteredOn(n -> n.depth() == 0)
			.extracting(CategoryNode::name)
			.containsExactlyInAnyOrder("국내도서", "외국도서");
		assertThat(tree).filteredOn(n -> n.name().equals("프로그래밍"))
			.singleElement()
			.satisfies(n -> {
				assertThat(n.depth()).isEqualTo(2);
				assertThat(n.path()).isEqualTo("국내도서 > IT > 프로그래밍");
			});
	}
}