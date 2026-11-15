package com.example.bookstore.config;

import java.time.Duration;

import org.jooq.ExecuteListenerProvider;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JooqConfig {

	/**
	 * ExecuteListenerProvider 빈은 Spring Boot가 자동으로 jOOQ 설정에 "추가"한다.
	 * (예외 변환 리스너 등 기존 리스너는 그대로 유지된다)
	 */
	@Bean
	ExecuteListenerProvider slowQueryListenerProvider(
		@Value("${bookstore.jooq.slow-query-threshold:200ms}")Duration threshold) {

		return new DefaultExecuteListenerProvider(new SlowQueryListener(threshold));
	}

	/** jOOQ Settings: SQL 렌더링과 실행 방식 조정 */
	@Bean
	DefaultConfigurationCustomizer jooqSettingsCustomizer() {
		return configuration -> configuration.settings()
			// 따옴표 삭제
			.withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
			.withRenderSchema(false)
			.withInListPadding(true)
			.withQueryTimeout(10);
	}
}
