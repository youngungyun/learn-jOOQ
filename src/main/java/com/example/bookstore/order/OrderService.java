package com.example.bookstore.order;

import static com.example.bookstore.jooq.Tables.*;
import static com.example.bookstore.order.OrderRepository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.common.ConflictException;
import com.example.bookstore.common.NotFoundException;
import com.example.bookstore.jooq.enums.OrderStatus;

@Service
public class OrderService {

	private final DSLContext dsl;
	private final OrderRepository orderRepository;

	public OrderService(DSLContext dsl, OrderRepository orderRepository) {
		this.dsl = dsl;
		this.orderRepository = orderRepository;
	}

	/**
	 * 주문 생성: 재고 차감 -> 주문 INSERT -> 항목 INSERT를 하나의 트랜잭션으로 처리한다.
	 * 어느 한 도서라도 재고가 부족하면 ConflictException으로 전체 롤백한다.
	 */
	@Transactional
	public OrderDetail placeOrder(PlaceOrderRequest request) {
		if (!dsl.fetchExists(MEMBER, MEMBER.ID.eq(request.memberId()))) {
			throw new NotFoundException("회원을 찾을 수 없습니다: id=" + request.memberId());
		}

		// 같은 도서가 여러 번 들어오면 합치고, bookId 순으로 정렬한다.
		// 모든 트랜잭션이 같은 순서로 행 락을 잡으므로 교차 락(데드락)을 예방한다.
		var quantities = new TreeMap<Long, Integer>();
		for (var item : request.items()) {
			quantities.merge(item.bookId(), item.quantity(), Integer::sum);
		}

		var lines = new ArrayList<NewOrderLine>();
		BigDecimal total = BigDecimal.ZERO;
		for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
			long bookId = entry.getKey();
			int quantity = entry.getValue();

			BigDecimal unitPrice = orderRepository.decreaseStock(bookId, quantity)
				.orElseThrow(() -> new ConflictException(
					"재고가 부족하거나 존재하지 않는 도서입니다: bookId=" + bookId
				));
			lines.add(new NewOrderLine(bookId, quantity, unitPrice));
			total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
		}

		long orderId = orderRepository.insertOrder(request.memberId(), total);
		orderRepository.insertItems(orderId, lines);
		return orderRepository.findDetail(orderId).orElseThrow();
	}

	/** 결제: PENDING -> PAID (조건부 UPDATE) */
	@Transactional
	public OrderDetail pay(long orderId) {
		if (!orderRepository.changeStatus(orderId, OrderStatus.PENDING,
			OrderStatus.PAID)) {
			throw orderRepository.existsById(orderId)
				? new ConflictException("결제 대기(PENDING) 상태의 주문만 결제할 수 있습니다.")
				: new NotFoundException("주문을 찾을 수 없습니다: id=" + orderId);
		}
		return orderRepository.findDetail(orderId).orElseThrow();
	}

	/**
	 * 취소: 주문 행을 FOR UPDATE로 잠근 뒤 상태를 확인하고, 재고를 복구한 다음 상태를 바꾼다.
	 * 동시에 두 번 취소해도 두 번째 요청은 락을 기다렸다가 CANCELLED를 보고 거부된다.
	 */
	@Transactional
	public OrderDetail cancel(long orderId) {
		var order = orderRepository.lockById(orderId)
			.orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: id" + orderId));

		if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.CANCELLED) {
			throw new ConflictException("취소할 수 없는 상태입니다: " + order.getStatus());
		}

		orderRepository.restoreStock(orderId);

		// 액티브 레코드: 변경된 status 컬럼만 UPDATE 한다.
		order.setStatus(OrderStatus.CANCELLED);
		order.store();

		return orderRepository.findDetail(orderId).orElseThrow();
	}

	@Transactional(readOnly = true)
	public OrderDetail get(long orderId) {
		return orderRepository.findDetail(orderId)
			.orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + orderId));
	}

	@Transactional(readOnly = true)
	public List<OrderSummary> listByMember(long memberId) {
		if (!dsl.fetchExists(MEMBER, MEMBER.ID.eq(memberId))) {
			throw new NotFoundException("회원을 찾을 수 없습니다: id" + memberId);
		}
		return orderRepository.findByMember(memberId);
	}
}
