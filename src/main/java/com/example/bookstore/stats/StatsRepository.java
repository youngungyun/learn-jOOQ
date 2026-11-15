package com.example.bookstore.stats;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

import com.example.bookstore.jooq.enums.OrderStatus;

@Repository
public class StatsRepository {

	private static final Condition REVENUE_ORDER =
		ORDERS.STATUS.in(OrderStatus.PAID, OrderStatus.SHIPPED);

	private final DSLContext dsl;

	public StatsRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * 월멸 매출 + 누적 매출 + 전월 매출,
	 *
	 * SELECT month, count(*), sum(total_amount),
	 * sum(sun(total_amount)) OVER (ORDER BY month),
	 * lag(sum(total_amount)) OVER (ORDER BY month)
	 * FROM orders WHERE status IN ('PAID', 'SHIPPED')
	 * GROUP BY month ORDER BY month;
	 */
	public List<MonthlySales> monthlySales() {
		var month = field(
			"cast(date_trunc('month', {0} at time zone 'Asia/Seoul') as date)",
			LocalDate.class, ORDERS.ORDERED_AT
		);
		var revenue = sum(ORDERS.TOTAL_AMOUNT);

		return dsl.select(
			month.as("month"),
			count().as("order_count"),
			revenue.as("revenue"),
			sum(revenue).over(orderBy(month)).as("cumulative_revenue"),
			lag(revenue).over(orderBy(month)).as("previous_month_revenue"))
			.from(ORDERS)
			.where(REVENUE_ORDER)
			.groupBy(month)
			.orderBy(month)
			.fetch(mapping(MonthlySales::new));
	}

	/**
	 * 카테고리별 베스트셀러 TOP N. CTE 두 단계:
	 * 1) book_sales: 도서별 판매 수량 집계
	 * 2) ranked: 카테고리 안에서 판매 수량 순위(rank)
	 * 마지막에 rank <= N으로 거른다.
	 */
	public List<BestSeller> bestSellerByCategory(int topN) {
		var sales = name("book_sales").fields("book_id", "sold_qty")
			.as(select(ORDER_ITEM.BOOK_ID, sum(ORDER_ITEM.QUANTITY))
			.from(ORDER_ITEM)
			.join(ORDERS).on(ORDERS.ID.eq(ORDER_ITEM.ORDER_ID))
			.where(REVENUE_ORDER)
			.groupBy(ORDER_ITEM.BOOK_ID));

		var salesBookId = sales.field("book_id", Long.class);
		var soldQty = sales.field("sold_qty", BigDecimal.class);

		var ranked = name("ranked").fields("category_name", "book_id", "title",
			"sold_qty", "rnk").as(
				select(CATEGORY.NAME, BOOK.ID, BOOK.TITLE, soldQty,
					rank().over(partitionBy(BOOK.CATEGORY_ID).orderBy(soldQty.desc())))
					.from(sales)
					.join(BOOK).on(BOOK.ID.eq(salesBookId))
					.join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID))
		);

		var categoryName = ranked.field("category_name", String.class);
		var bookId = ranked.field("book_id", Long.class);
		var title = ranked.field("title", String.class);
		var rankedQty = ranked.field("sold_qty", BigDecimal.class);
		var rnk = ranked.field("rnk", Integer.class);

		return dsl.with(sales, ranked)
			.select(categoryName, bookId, title, rankedQty, rnk)
			.from(ranked)
			.where(rnk.le(topN))
			.orderBy(categoryName, rnk, bookId)
			.fetch(mapping(BestSeller::new));
	}

	/** 주문 상태별 건수. enum 타입 그대로 Map의 키가 된다.*/
	public Map<OrderStatus, Integer> orderStatusCounts() {
		return dsl.select(ORDERS.STATUS, count())
			.from(ORDERS)
			.groupBy(ORDERS.STATUS)
			.orderBy(ORDERS.STATUS)
			.fetchMap(Record2::value1, Record2::value2);
	}
}
