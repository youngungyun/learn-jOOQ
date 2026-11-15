package com.example.bookstore.stats;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.jooq.enums.OrderStatus;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

	private final StatsRepository statsRepository;

	public StatsController(StatsRepository statsRepository) {
		this.statsRepository = statsRepository;
	}

	@GetMapping("/monthly-sales")
	public List<MonthlySales> monthlySales() {
		return statsRepository.monthlySales();
	}

	@GetMapping("/best-sellers")
	public List<BestSeller> bestSellers(@RequestParam(defaultValue = "3") int top) {
		return statsRepository.bestSellersByCategory(
			Math.clamp(top, 1, 50)
		);
	}

	@GetMapping("/order-status")
	public Map<OrderStatus, Integer> orderStatus() {
		return statsRepository.orderStatusCounts();
	}
}
