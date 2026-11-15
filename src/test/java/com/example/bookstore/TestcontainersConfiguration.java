package com.example.bookstore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 18 컨테이너
 * @ServiceConnection이 컨테이너의 JDBC URL/계정을 spring.datasource.* 대신 자동으로 연결한다.
 * (Testcontainers 2.0부터 PostgreSQLContainer는
 * org.testcontainers.postgresql 패키지이며 제네릭이 없다)
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgreSQLContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));
	}
}
