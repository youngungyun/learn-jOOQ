package com.example.bookstore.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlySales(
	LocalDate month,
	Integer orderCount,
	BigDecimal revenue,
	BigDecimal cumulativeRevenue,
	BigDecimal previousMonthRevenue
) {
}
