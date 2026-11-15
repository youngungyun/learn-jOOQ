package com.example.bookstore.config;

import java.time.Duration;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 임계값보다 오래 걸린 쿼리를 WARN으로 남긴다.
 * ExecuteListener는 실행마다 같은 인스턴스가 호출되므로 상태를 필드에 두면 안 되고,
 * 실행별 데이터는 ExecuteListener에 담는다.
 */
public class SlowQueryListener implements ExecuteListener {

	private static final Logger log = LoggerFactory.getLogger(SlowQueryListener.class);
	private static final String START_NANOS = SlowQueryListener.class.getName() + ".start";

	private final long thresholdNanos;

	public SlowQueryListener(Duration threshold) {
		this.thresholdNanos = threshold.toNanos();
	}

	@Override
	public void executeStart(ExecuteContext ctx) {
		ctx.data(START_NANOS, System.nanoTime());
	}

	@Override
	public void executeEnd(ExecuteContext ctx) {
		if (!(ctx.data(START_NANOS) instanceof Long start)) {
			return;
		}
		long elapsed = System.nanoTime() - start;
		if (elapsed >= thresholdNanos) {
			log.warn("느린 쿼리 {}ms: {}", elapsed / 1_000_000, ctx.sql());
		}
	}
}
