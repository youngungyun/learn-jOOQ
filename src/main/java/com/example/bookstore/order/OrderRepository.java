package com.example.bookstore.order;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

import com.example.bookstore.jooq.enums.OrderStatus;
import com.example.bookstore.jooq.tables.records.OrdersRecord;

@Repository
public class OrderRepository {

	/** 주문 생성 시 한 줄 (단가는 재고 차감 시점의 도서 가격) */
	public record NewOrderLine(long bookId, int quantity, BigDecimal unitPrice) {
	}

	private final DSLContext dsl;

	public OrderRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * 원자적 재고 차감. 재고가 충분할 때만 감소시키고 현재 가격을 돌려준다.
	 */
	public Optional<BigDecimal> decreaseStock(long bookId, int quantity) {
		return dsl.update(BOOK)
			.set(BOOK.STOCK, BOOK.STOCK.minus(quantity))
			.where(BOOK.ID.eq(bookId))
			.and(BOOK.STOCK.ge(quantity))
			.returningResult(BOOK.PRICE)
			.fetchOptional()
			.map(Record1::value1);
	}

	/** 취소된 주문의 수량만큼 재고를 되돌린다. */
	public int restoreStock(long orderId) {
		return dsl.update(BOOK)
			.set(BOOK.STOCK, BOOK.STOCK.plus(ORDER_ITEM.QUANTITY))
			.from(ORDER_ITEM)
			.where(ORDER_ITEM.BOOK_ID.eq(BOOK.ID))
			.and(ORDER_ITEM.ORDER_ID.eq(orderId))
			.execute();
	}

	public long insertOrder(long memberId, BigDecimal totalAmount) {
		return dsl.insertInto(ORDERS)
			.set(ORDERS.MEMBER_ID, memberId)
			.set(ORDERS.TOTAL_AMOUNT, totalAmount)
			.returningResult(ORDERS.ID)
			.fetchOne()
			.value1();
	}

	public void insertItems(long orderId, List<NewOrderLine> lines) {
		var insert = dsl.insertInto(ORDER_ITEM,
			ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.QUANTITY,
			ORDER_ITEM.UNIT_PRICE);
		for (var line : lines) {
			insert = insert.values(orderId, line.bookId(), line.quantity(), line.unitPrice());
		}
		insert.execute();
	}

	/** 비관적 락; FOR UPDATE */
	public Optional<OrdersRecord> lockById(long orderId) {
		return dsl.selectFrom(ORDERS)
			.where(ORDERS.ID.eq(orderId))
			.forUpdate()
			.fetchOptional();
	}

	/** 상태 전이도 조건부 UPDATE: 현재 상태가 from 일 때만 to로 바꾼다. */
	public boolean changeStatus(long orderId, OrderStatus from, OrderStatus to) {
		return dsl.update(ORDERS)
			.set(ORDERS.STATUS, to)
			.where(ORDERS.ID.eq(orderId))
			.and(ORDERS.STATUS.eq(from))
			.execute() == 1;
	}

	public boolean existsById(long orderId) {
		return dsl.fetchExists(ORDERS, ORDERS.ID.eq(orderId));
	}

	/** 주문 + 회원 이름 + 주문 항목(MULTISET)을 한 번에 읽는다. */
	public Optional<OrderDetail> findDetail(long orderId) {
		var items = multiset(
			select(ORDER_ITEM.BOOK_ID,
				BOOK.TITLE,
				ORDER_ITEM.QUANTITY,
				ORDER_ITEM.UNIT_PRICE,
				ORDER_ITEM.UNIT_PRICE.mul(ORDER_ITEM.QUANTITY))
				.from(ORDER_ITEM)
				.join(BOOK).on(BOOK.ID.eq(ORDER_ITEM.BOOK_ID))
				.where(ORDER_ITEM.ORDER_ID.eq(orderId))
				.orderBy(ORDER_ITEM.ID)
		).as("items")
			.convertFrom(result -> result.map(mapping(OrderLine::new)));

		return dsl.select(ORDERS.ID, ORDERS.MEMBER_ID, MEMBER.NAME,
			ORDERS.STATUS, ORDERS.TOTAL_AMOUNT, ORDERS.ORDERED_AT, items)
			.from(ORDERS)
			.join(MEMBER).on(MEMBER.ID.eq(ORDERS.MEMBER_ID))
			.where(ORDERS.ID.eq(orderId))
			.fetchOptional(mapping(OrderDetail::new));
	}

	/** 회원의 주문 목록. 항목 수는 스칼라 서브쿼리로 센다. */
	public List<OrderSummary> findByMember(long memberId) {
		var itemCount = field(
			selectCount().from(ORDER_ITEM).where(ORDER_ITEM.ORDER_ID.eq(ORDERS.ID))
		);

		return dsl.select(ORDERS.ID, ORDERS.STATUS, ORDERS.TOTAL_AMOUNT,
			ORDERS.ORDERED_AT, itemCount)
			.from(ORDERS)
			.where(ORDERS.MEMBER_ID.eq(memberId))
			.orderBy(ORDERS.ORDERED_AT.desc())
			.fetch(mapping(OrderSummary::new));
	}
}
