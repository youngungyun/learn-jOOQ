package com.example.bookstore.order;

import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static com.example.bookstore.jooq.Tables.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookstore.TestcontainersConfiguration;
import com.example.bookstore.common.ConflictException;
import com.example.bookstore.jooq.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * 트랜잭션 롤백과 동시성을 검증해야 하므로 @Transactional 을 붙이지 않는다.
 * 대신 테스트마다 새 도서를 만들어 다른 테스트와 데이터가 섞이지 않게 한다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderServiceTest {

	private static final AtomicLong ISBN_SEQUENCE = new AtomicLong(9_990_000_000_000L);

	@Autowired
	OrderService orderService;

	@Autowired
	DSLContext dsl;

	private long memberId;

	@BeforeEach
	void setUp() {
		memberId = dsl.select(MEMBER.ID)
			.from(MEMBER)
			.where(MEMBER.EMAIL.eq("minsu@example.com"))
			.fetchSingle()
			.value1();
	}

	@Test
	@DisplayName("주문하면 재고가 차감되고 합계가 단가 × 수량으로 계산된다")
	void placeOrder() {
		long bookId = createBook(5, "10000.00");

		OrderDetail order = orderService.placeOrder(request(bookId, 2));

		assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.totalAmount()).isEqualByComparingTo("20000");
		assertThat(order.items()).singleElement().satisfies(line -> {
			assertThat(line.quantity()).isEqualTo(2);
			assertThat(line.lineAmount()).isEqualByComparingTo("20000");
		});
		assertThat(stockOf(bookId)).isEqualTo(3);
	}

	@Test
	@DisplayName("한 도서라도 재고가 부족하면 이미 차감한 다른 도서의 재고까지 롤백된다")
	void rollbackWhenOutOfStock() {
		long enough = createBook(5, "10000.00");
		long empty = createBook(0, "10000.00");

		PlaceOrderRequest request = new PlaceOrderRequest(memberId, List.of(
			new PlaceOrderRequest.Item(enough, 1),
			new PlaceOrderRequest.Item(empty, 1)));

		assertThatThrownBy(() -> orderService.placeOrder(request))
			.isInstanceOf(ConflictException.class);
		assertThat(stockOf(enough)).isEqualTo(5);
	}

	@Test
	@DisplayName("동시에 20명이 주문해도 재고(5권)보다 많이 팔리지 않는다")
	void concurrentOrdersNeverOversell() throws Exception {
		long bookId = createBook(5, "10000.00");
		int threads = 20;
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger success = new AtomicInteger();
		AtomicInteger conflict = new AtomicInteger();

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			for (int i = 0; i < threads; i++) {
				executor.submit(() -> {
					ready.countDown();
					start.await();          // 모두 준비될 때까지 기다렸다가 동시에 출발
					try {
						orderService.placeOrder(request(bookId, 1));
						success.incrementAndGet();
					} catch (ConflictException e) {
						conflict.incrementAndGet();
					}
					return null;
				});
			}
			ready.await();
			start.countDown();
		} // close() 가 모든 작업의 종료를 기다린다

		assertThat(success.get() + conflict.get()).isEqualTo(threads);   // 예상 밖의 예외가 없었는지
		assertThat(success.get()).isEqualTo(5);
		assertThat(stockOf(bookId)).isZero();
	}

	@Test
	@DisplayName("결제는 PENDING 에서만, 취소는 재고를 복구하고 두 번 할 수 없다")
	void payAndCancel() {
		long bookId = createBook(5, "10000.00");
		OrderDetail order = orderService.placeOrder(request(bookId, 2));

		assertThat(orderService.pay(order.id()).status()).isEqualTo(OrderStatus.PAID);
		assertThatThrownBy(() -> orderService.pay(order.id())).isInstanceOf(ConflictException.class);

		assertThat(orderService.cancel(order.id()).status()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(stockOf(bookId)).isEqualTo(5);
		assertThatThrownBy(() -> orderService.cancel(order.id())).isInstanceOf(ConflictException.class);
	}

	// ----- 헬퍼 -----

	private PlaceOrderRequest request(long bookId, int quantity) {
		return new PlaceOrderRequest(memberId, List.of(new PlaceOrderRequest.Item(bookId, quantity)));
	}

	private long createBook(int stock, String price) {
		long categoryId = dsl.select(CATEGORY.ID)
			.from(CATEGORY)
			.where(CATEGORY.NAME.eq("프로그래밍"))
			.fetchSingle()
			.value1();
		return dsl.insertInto(BOOK)
			.set(BOOK.CATEGORY_ID, categoryId)
			.set(BOOK.TITLE, "테스트 도서")
			.set(BOOK.ISBN, String.valueOf(ISBN_SEQUENCE.incrementAndGet()))
			.set(BOOK.PRICE, new BigDecimal(price))
			.set(BOOK.STOCK, stock)
			.returningResult(BOOK.ID)
			.fetchOne()
			.value1();
	}

	private int stockOf(long bookId) {
		return dsl.select(BOOK.STOCK)
			.from(BOOK)
			.where(BOOK.ID.eq(bookId))
			.fetchSingle()
			.value1();
	}
}