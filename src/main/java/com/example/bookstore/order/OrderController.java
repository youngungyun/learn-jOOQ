package com.example.bookstore.order;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderDetail> place(@Valid @RequestBody PlaceOrderRequest request) {
		var order = orderService.placeOrder(request);
		return ResponseEntity.created(URI.create("/api/orders/" + order.id()))
			.body(order);
	}

	@GetMapping("/{id}")
	public OrderDetail get(@PathVariable long id) {
		return orderService.get(id);
	}

	@GetMapping(params = "memberId")
	public List<OrderSummary> listByMember(@RequestParam long memberId) {
		return orderService.listByMember(memberId);
	}

	@PostMapping("/{id}/pay")
	public OrderDetail pay(@PathVariable long id) {
		return orderService.pay(id);
	}

	@PostMapping("/{id}/cancel")
	public OrderDetail cancel(@PathVariable long id) {
		return orderService.cancel(id);
	}
}
