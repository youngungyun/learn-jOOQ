# jOOQ 학습 커리큘럼 — Spring Boot 4 · PostgreSQL · Flyway로 도서 주문 시스템 완성하기

Nov 8, 2026 · @Yun

## 0. 개요 — 무엇을, 어떤 순서로 배우는가

이 커리큘럼은 13개 장에 걸쳐 **"북스토어 주문 시스템"** 한 개를 처음부터 끝까지 완성하면서 jOOQ 3.21의 핵심 기능을 익히도록 구성했습니다. 각 장은 앞 장의 코드 위에 파일을 추가하는 방식이며, 모든 파일은 **전문(全文)** 으로 실려 있어 그대로 따라 치면 동작하는 프로젝트가 됩니다.

### 왜 jOOQ인가

jOOQ는 데이터베이스 스키마에서 Java 코드를 생성하고, 그 코드로 **타입 안전한 SQL** 을 Java 문법으로 작성하게 해 주는 라이브러리입니다. ORM처럼 SQL을 숨기지 않고, MyBatis처럼 SQL을 문자열로 다루지도 않는다는 점이 가장 큰 특징입니다.

| 관점                   | JPA/Hibernate     | MyBatis          | jOOQ                       |
| -------------------- | ----------------- | ---------------- | -------------------------- |
| 사고방식                 | 객체 그래프 중심         | SQL 문자열 중심       | SQL 중심 + 타입 안전             |
| 컴파일 타임 검증            | JPQL은 대부분 런타임 검증  | XML/문자열이라 런타임 검증 | 컬럼·타입 오류가 컴파일 에러           |
| 복잡한 SQL(윈도우 함수, CTE) | 네이티브 쿼리로 우회       | 가능하지만 문자열        | DSL로 1급 지원                 |
| 동적 쿼리                | Criteria API(장황함) | `<if>` 태그        | `Condition` 객체 조합          |
| 스키마 변경 대응            | 엔티티 수동 수정         | 매퍼 수동 수정         | 코드 재생성 → 깨진 곳이 컴파일 에러로 드러남 |
| 잘 맞는 곳               | 도메인 모델이 풍부한 CRUD  | 레거시 SQL 이관       | 조회·통계가 많은 서비스, SQL을 잘 쓰는 팀 |

### 기술 스택과 버전 (작성일 기준 최신 안정 버전)

| 구분 | 버전 | 비고 |
| --- | --- | --- |
| Java | 25 (LTS) | jOOQ 3.20부터 오픈소스 에디션은 Java 21 이상이 필요합니다 |
| Gradle | 9.x (9.6.1 이상 권장) | Kotlin DSL(`build.gradle.kts`) 사용 |
| Spring Boot | 4.1.2 | 2026-10-22 릴리스. 4.0 대비 OSS 지원 기간이 2027-07까지로 깁니다 |
| jOOQ (Open Source Edition) | 3.21.9 | 공식 Gradle 플러그인 `org.jooq.jooq-codegen-gradle` 사용 |
| Flyway | 12.x | Spring Boot 4부터는 `spring-boot-starter-flyway` 가 필요합니다 |
| PostgreSQL | 18 | 도커 이미지 `postgres:18` |
| PostgreSQL JDBC | 42.7.x |  |
| Testcontainers | 2.0.x | Spring Boot가 버전 관리. 2.0부터 패키지·아티팩트 이름이 바뀌었습니다 |

버전은 2026-11-08 기준으로 확인했습니다. Spring Boot 4.2는 현재 RC 단계로 11월 중 GA가 예정되어 있으니, 정식 출시 후에는 `build.gradle.kts` 의 플러그인 버전만 바꿔 올리면 됩니다. 패치 버전은 학습 시점에 [Maven Central](https://central.sonatype.com/)에서 한 번 더 확인하세요.

### 완성할 프로젝트: 북스토어 주문 시스템

| 기능 영역 | 주요 API | 배우는 jOOQ 기능 |
| --- | --- | --- |
| 저자 관리 | `GET/POST/PUT/DELETE /api/authors` | 기본 CRUD, `fetchOptional`, `returning` |
| 도서 조회 | `GET /api/books/{id}`, `/api/books?category=` | 명시적 조인, 암시적(path) 조인, `Records.mapping` |
| 도서 검색 | `GET /api/books/search`, `/api/books/scroll` | 동적 `Condition`, 오프셋·키셋 페이지네이션 |
| 도서 상세 | `GET /api/books/{id}/detail` | `MULTISET`, 중첩 `row()` 매핑 |
| 도서 등록·가격 변경 | `POST /api/books`, `PATCH /api/books/{id}/price` | 트랜잭션, 다중 행 INSERT, 수동 낙관적 락 |
| 주문 | `POST /api/orders`, `/pay`, `/cancel` | 원자적 재고 차감, `FOR UPDATE`, `UPDATE ... FROM`, `UpdatableRecord.store()` |
| 회원·태그·속성 | `PUT /api/members`, `/api/books/{id}/tags/{tag}` | `ON CONFLICT` 업서트, 배열 함수, JSONB, `Converter` |
| 통계·카테고리 | `/api/stats/*`, `/api/categories/tree` | 집계, 윈도우 함수, CTE, 재귀 CTE |

도메인 테이블은 7개입니다: `category`(자기 참조 계층), `author`, `book`, `book_author`(도서-저자 N:M), `member`, `orders`, `order_item`.

### 학습 로드맵

| 장 | 주제 | 핵심 개념 | 이 장의 산출물 |
| --- | --- | --- | --- |
| 1 | 프로젝트와 개발 환경 | Gradle, Docker Compose, Boot 4 모듈 구조 | 빌드 파일, 설정 파일 |
| 2 | Flyway 스키마 | 버전 마이그레이션, 시드 데이터 | V1\~V3 SQL |
| 3 | jOOQ 코드 생성 | 생성 클래스 구조, forcedType, Converter | 생성 코드 |
| 4 | 기초 CRUD | `DSLContext`, Record, fetch 계열 | 저자 API |
| 5 | 조인과 매핑 | 조인, path 조인, 집계 배열 | 도서 조회 API |
| 6 | 동적 쿼리·페이지네이션 | `noCondition()`, row value 비교 | 검색 API |
| 7 | MULTISET | 중첩 컬렉션 한 방 쿼리 | 도서 상세 API |
| 8 | 트랜잭션·쓰기 | `@Transactional`, 락, 동시성 | 도서 등록·주문 API |
| 9 | PostgreSQL 특화 | enum, 배열, JSONB, 업서트 | 회원·태그 API |
| 10 | 고급 SQL | 윈도우 함수, CTE, 재귀 CTE | 통계 API |
| 11 | 설정과 관측 | `Settings`, `ExecuteListener`, 예외 변환 | 설정 클래스 |
| 12 | 테스트 | Testcontainers, `@ServiceConnection` | 테스트 코드 |
| 13 | 마무리 | 최종 구조, 체크리스트 | 회고 |

### 학습 방법

- **사전 지식**: Java 17+ 문법(record, `var`, switch 식), Spring Boot로 REST API를 만들어 본 경험, 기본 SQL(JOIN, GROUP BY).
- **준비물**: JDK 25, Docker Desktop(또는 호환 런타임), IntelliJ IDEA.
- **진행 방식**: 각 장의 "학습 목표 → 개념 → 코드 → 코드 해설 → 실행 확인 → 연습 과제" 순서로 읽습니다. 연습 과제는 다음 장으로 넘어가기 전에 꼭 직접 풀어 보세요. API 동작은 1-8에서 만드는 API 콘솔 페이지에서 확인합니다.
- **SQL을 늘 확인하기**: jOOQ는 "SQL을 Java로 쓰는 도구"이므로, 1장에서 켜 두는 SQL 로그를 보며 내가 의도한 SQL이 나가는지 항상 확인하는 습관이 가장 중요합니다.

## 1장. 프로젝트 생성과 개발 환경

이 장이 끝나면 PostgreSQL 18이 도커로 뜨고, jOOQ 코드 생성과 Flyway까지 준비된 Spring Boot 4.1 프로젝트 뼈대가 완성됩니다. 빌드 파일은 최종본을 미리 작성하므로 이후 장에서는 다시 고치지 않습니다.

### 학습 목표

- Spring Boot 4의 모듈화된 스타터 구조(`spring-boot-starter-jooq`, `spring-boot-starter-flyway`)를 이해한다.
- jOOQ 코드 생성 파이프라인(로컬 DB → Flyway 마이그레이션 → 코드 생성)을 설계한다.
- Spring Boot가 관리하는 라이브러리 버전을 덮어쓰는 방법을 익힌다.

### 1-1. 프로젝트 만들기

[start.spring.io](https://start.spring.io)에서 다음과 같이 생성한 뒤, 아래 파일들로 덮어쓰면 가장 빠릅니다.

- Project: **Gradle - Kotlin**, Language: **Java**, Spring Boot: **4.1.2**
- Group: `com.example`, Artifact: `bookstore`, Package name: `com.example.bookstore`
- Java: **25**
- Dependencies: Spring Web, Validation, JOOQ Access Layer, Flyway Migration, PostgreSQL Driver, Testcontainers

Initializr 없이 직접 만든다면 빈 디렉터리에 아래 파일을 만든 뒤 `gradle wrapper --gradle-version 9.6.1` 로 래퍼를 생성하세요(로컬에 Gradle 9.x가 설치되어 있어야 합니다).

최종 디렉터리 구조는 13장에 정리되어 있고, 이 장에서 만드는 파일은 다음과 같습니다.

```text
bookstore/
├── settings.gradle.kts
├── build.gradle.kts
├── compose.yaml
├── .gitignore
└── src/main/
    ├── java/com/example/bookstore/BookstoreApplication.java
    └── resources/application.yml
```

### 1-2. `settings.gradle.kts`

```kotlin
rootProject.name = "bookstore"
```

로컬에 JDK 25가 없다면 이 파일 맨 위에 `plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }` 를 추가하세요. Gradle이 JDK를 자동으로 내려받습니다.

### 1-3. `build.gradle.kts` (최종본)

```kotlin
plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    // jOOQ 공식 코드 생성 플러그인. 런타임 jOOQ 버전과 반드시 같아야 한다.
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Spring Boot BOM이 관리하는 기본 jOOQ 버전 대신 코드 생성 플러그인과 같은 3.21.9를 쓴다.
extra["jooq.version"] = "3.21.9"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // 코드 생성기가 DB에 접속할 때 쓰는 JDBC 드라이버
    jooqCodegen("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// jOOQ 코드 생성: 로컬 도커 DB에 접속해 스키마를 읽는다.
jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:5432/bookstore"
            user = "bookstore"
            password = "bookstore"
        }
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                // Flyway 이력 테이블은 애플리케이션 코드에서 쓸 일이 없다.
                excludes = "flyway_schema_history"
                forcedTypes {
                    // book.attributes(JSONB) 컬럼을 Java record로 다루기 위한 변환기 (3장)
                    forcedType {
                        userType = "com.example.bookstore.book.BookAttributes"
                        converter = "com.example.bookstore.book.BookAttributesConverter"
                        includeExpression = "public\\.book\\.attributes"
                    }
                }
            }
            target {
                packageName = "com.example.bookstore.jooq"
                // 생성 코드는 build/ 가 아닌 여기에 두고 Git에 커밋한다.
                directory = "src/generated/java"
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("src/generated/java")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

**코드 해설**

- **Spring Boot 4의 모듈화**: Boot 4부터 자동 설정이 기술별 모듈로 쪼개졌습니다. 예전에는 `flyway-core` 만 추가해도 Flyway가 자동 실행되었지만, 이제는 `spring-boot-starter-flyway` 가 있어야 합니다. 웹도 `spring-boot-starter-web` 대신 `spring-boot-starter-webmvc` 를 씁니다. 테스트 슬라이스 역시 `spring-boot-starter-webmvc-test`, `spring-boot-starter-jooq-test` 처럼 분리되어 있습니다.
- **`flyway-database-postgresql`**: Flyway 10부터 DB별 지원 코드가 별도 모듈로 분리되었습니다. 빠뜨리면 "Unsupported Database" 오류가 납니다.
- **`extra["jooq.version"]`**: Spring Boot BOM이 정한 jOOQ 버전을 덮어씁니다. 코드 생성 플러그인 버전(3.21.9)과 런타임 버전이 다르면 생성 코드가 컴파일되지 않거나 시작 시 경고가 나므로 반드시 같게 맞춥니다. 확인은 `./gradlew dependencyInsight --dependency org.jooq:jooq` 로 합니다.
- **코드 생성 전략**: jOOQ 코드 생성에는 크게 세 가지 방식이 있습니다. ① 실제 DB에 접속(이 커리큘럼의 방식), ② SQL 파일을 파싱하는 `DDLDatabase`, ③ 빌드 때마다 Testcontainers로 임시 DB를 띄우는 방식. ①은 PostgreSQL 고유 문법(enum, JSONB, 배열)을 100% 정확하게 반영하고 설정이 가장 단순합니다. 대신 생성 결과를 커밋해 CI나 동료의 빌드에는 DB가 필요 없게 했고, 스키마 변경이 코드 리뷰에도 드러납니다.
- **`jooq { }` 블록에는 꼭 필요한 것만**: 접속 정보(`jdbc`), 읽을 DB 종류와 스키마(`database`), 제외 테이블, JSONB 변환기(`forcedType`), 생성 위치(`target`)만 적었습니다. 테이블별 `XxxRecord` 클래스, `LocalDate`·`OffsetDateTime` 같은 `java.time` 타입, 5장의 path 조인 메서드는 jOOQ가 기본으로 생성하므로 따로 켜지 않아도 됩니다. POJO와 DAO는 기본으로 꺼져 있고, 대신 API 응답용 Java record를 직접 정의합니다(생성 코드와 API 계약을 분리하기 위함).
- **`sourceSets`**: `src/generated/java` 를 일반 소스처럼 컴파일하도록 등록합니다.
- **코드 생성 순서**: Gradle은 마이그레이션을 돌리지 않습니다. 앱을 한 번 띄워 Spring Boot의 Flyway가 스키마를 만든 뒤, `./gradlew jooqCodegen` 으로 코드를 생성합니다. 자세한 절차는 3장에서 다룹니다.

### 1-4. `compose.yaml`

```yaml
services:
  postgres:
    image: postgres:18
    container_name: bookstore-postgres
    environment:
      POSTGRES_DB: bookstore
      POSTGRES_USER: bookstore
      POSTGRES_PASSWORD: bookstore
      TZ: Asia/Seoul
    ports:
      - "5432:5432"
    volumes:
      # PostgreSQL 18 이미지부터는 /var/lib/postgresql 을 마운트한다 (버전별 하위 디렉터리가 자동 생성됨)
      - bookstore-pgdata:/var/lib/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U bookstore -d bookstore"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  bookstore-pgdata:
```

PostgreSQL 18 공식 이미지는 데이터 디렉터리 기본 위치가 바뀌었습니다. 예전처럼 `/var/lib/postgresql/data` 에 볼륨을 붙이면 컨테이너가 뜨지 않으니 위와 같이 상위 디렉터리를 마운트합니다.

### 1-5. `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: bookstore
  datasource:
    url: jdbc:postgresql://localhost:5432/bookstore
    username: bookstore
    password: bookstore
    hikari:
      maximum-pool-size: 10
  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
  jooq:
    sql-dialect: postgres
  threads:
    virtual:
      enabled: true

bookstore:
  jooq:
    # 11장의 느린 쿼리 로깅 기준
    slow-query-threshold: 200ms

logging:
  level:
    # jOOQ가 실행하는 SQL과 결과 일부를 DEBUG로 출력한다 (학습 중에는 항상 켜 둔다)
    org.jooq.tools.LoggerListener: debug
    org.flywaydb: info
```

- `spring.jooq.sql-dialect`: jOOQ가 어떤 DB 문법으로 SQL을 렌더링할지 정합니다. 생략하면 JDBC URL에서 추론하지만 명시하는 편이 안전합니다.
- `org.jooq.tools.LoggerListener: debug`: jOOQ가 만든 SQL을 바인드 값이 채워진 형태로 로그에 남깁니다. jOOQ 학습의 가장 중요한 도구입니다.
- 기동 시 출력되는 jOOQ 로고와 팁 메시지가 거슬리면 JVM 옵션 `-Dorg.jooq.no-logo=true -Dorg.jooq.no-tips=true` 를 줍니다.

### 1-6. `src/main/java/com/example/bookstore/BookstoreApplication.java`

```java
package com.example.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }
}
```

### 1-7. `.gitignore`

```text
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
.idea/
*.iml
out/
.DS_Store
# src/generated/java 는 커밋 대상이므로 무시하지 않는다
```

### 1-8. `src/main/resources/static/index.html` — API 콘솔

4장부터 각 장의 "실행 확인"은 터미널의 curl 대신 이 페이지에서 합니다. 왼쪽에서 API를 고르고 경로 변수·쿼리·JSON 본문을 채워 보내면, 오른쪽에 상태 코드와 응답 JSON이 보입니다.

- **접속**: `./gradlew bootRun` 후 http://localhost:8080/ 을 엽니다. Spring Boot는 `static/` 아래 파일을 그대로 서빙하므로 API와 같은 origin이 되어 CORS 설정이 필요 없습니다.
- **기본값**: 입력칸에는 각 장 실행 확인에서 쓰는 값이 미리 들어 있습니다. 바꾼 값과 최근 요청은 브라우저에 저장되고, **초기화**로 되돌립니다.
- **오류 응답**: 4장의 ProblemDetail 응답은 title과 detail을 따로 강조해 보여 줍니다.
- **curl 미리보기**: 같은 요청의 curl 명령을 함께 보여 줍니다. CLI로도 해 보고 싶으면 복사해 쓰면 됩니다.
- **아직 만들지 않은 API**: 목록에는 4\~10장 API가 모두 있으므로, 아직 진행하지 않은 장의 API는 404가 납니다. API를 직접 추가했다면 파일 위쪽 `CATALOG` 배열에 한 줄 추가합니다.

```html
<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Bookstore API 콘솔</title>
<!--
  위치: src/main/resources/static/index.html  (커리큘럼 1-8)
  ./gradlew bootRun 후 http://localhost:8080/ 으로 접속한다.
  Spring Boot가 static/ 아래 파일을 그대로 서빙하므로 같은 origin → CORS 설정이 필요 없다.
-->
<style>
  :root {
    --bg: #f6f7f9; --panel: #ffffff; --line: #e3e6eb; --text: #1d2330; --muted: #6b7385;
    --accent: #2f6fed; --accent-weak: #e8effd;
    --get: #1f8a4c; --post: #2f6fed; --put: #a05a00; --patch: #7a45c9; --delete: #c83a3a;
    --ok: #1f8a4c; --warn: #a05a00; --err: #c83a3a;
    --code-bg: #f3f5f8; --k: #7a45c9; --s: #1f7a52; --n: #b3541e; --b: #2f6fed;
  }
  @media (prefers-color-scheme: dark) {
    :root {
      --bg: #12151b; --panel: #1a1e26; --line: #2b313c; --text: #e6e9ef; --muted: #8d95a6;
      --accent: #6b9bff; --accent-weak: #1f2a40;
      --get: #4cc583; --post: #6b9bff; --put: #e0a24a; --patch: #b48cf0; --delete: #f07878;
      --ok: #4cc583; --warn: #e0a24a; --err: #f07878;
      --code-bg: #141820; --k: #b48cf0; --s: #7fd3a6; --n: #f0a670; --b: #6b9bff;
    }
  }
  * { box-sizing: border-box; }
  body { margin: 0; background: var(--bg); color: var(--text);
         font: 14px/1.5 -apple-system, BlinkMacSystemFont, "Pretendard", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; }
  code, pre, textarea, .mono { font-family: ui-monospace, "JetBrains Mono", "D2Coding", Menlo, Consolas, monospace; font-size: 13px; }
  header { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-bottom: 1px solid var(--line); background: var(--panel); }
  header h1 { font-size: 15px; margin: 0; font-weight: 650; }
  header .base { margin-left: auto; display: flex; align-items: center; gap: 6px; color: var(--muted); font-size: 12px; }
  header .base input { width: 220px; }
  .layout { display: grid; grid-template-columns: 290px 1fr; height: calc(100vh - 49px); }
  nav { border-right: 1px solid var(--line); overflow-y: auto; background: var(--panel); }
  nav .filter { position: sticky; top: 0; padding: 10px; background: var(--panel); border-bottom: 1px solid var(--line); }
  nav .filter input { width: 100%; }
  nav h2 { font-size: 11px; letter-spacing: .04em; color: var(--muted); margin: 14px 12px 4px; font-weight: 600; }
  nav button.ep { display: flex; align-items: center; gap: 8px; width: 100%; border: 0; background: none; color: inherit;
                  padding: 6px 12px; text-align: left; cursor: pointer; font: inherit; }
  nav button.ep:hover { background: var(--accent-weak); }
  nav button.ep.active { background: var(--accent-weak); box-shadow: inset 3px 0 0 var(--accent); }
  nav .path { font-size: 12.5px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  nav .label { display: block; color: var(--muted); font-size: 11.5px; }
  .m { display: inline-block; min-width: 50px; text-align: center; font-size: 10.5px; font-weight: 700; padding: 1px 0;
       border-radius: 4px; color: #fff; flex: none; }
  .m.GET { background: var(--get); } .m.POST { background: var(--post); } .m.PUT { background: var(--put); }
  .m.PATCH { background: var(--patch); } .m.DELETE { background: var(--delete); }
  main { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); overflow: hidden; }
  section { overflow-y: auto; padding: 16px 20px; }
  section + section { border-left: 1px solid var(--line); }
  .title { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
  .title code { font-size: 15px; font-weight: 600; word-break: break-all; }
  .desc { color: var(--muted); margin: 0 0 16px; }
  h3 { font-size: 12px; color: var(--muted); font-weight: 600; margin: 18px 0 6px; letter-spacing: .03em; }
  .field { display: grid; grid-template-columns: 130px 1fr; gap: 8px; align-items: center; margin-bottom: 6px; }
  .field label { font-family: ui-monospace, Menlo, Consolas, monospace; font-size: 12.5px; }
  .field label small { display: block; color: var(--muted); font-family: inherit; font-size: 11px; }
  input, select, textarea { background: var(--panel); color: var(--text); border: 1px solid var(--line); border-radius: 6px; padding: 6px 8px; font: inherit; }
  input:focus, select:focus, textarea:focus { outline: 2px solid var(--accent); outline-offset: -1px; border-color: transparent; }
  textarea { width: 100%; min-height: 220px; resize: vertical; tab-size: 2; }
  .actions { display: flex; gap: 8px; align-items: center; margin-top: 14px; flex-wrap: wrap; }
  button.primary, button.ghost { border-radius: 6px; padding: 7px 14px; font: inherit; font-weight: 600; cursor: pointer; }
  button.primary { background: var(--accent); color: #fff; border: 1px solid var(--accent); }
  button.primary:disabled { opacity: .6; cursor: progress; }
  button.ghost { background: none; color: var(--text); border: 1px solid var(--line); font-weight: 500; }
  button.ghost:hover { border-color: var(--accent); }
  .hint { color: var(--muted); font-size: 12px; }
  pre { background: var(--code-bg); border: 1px solid var(--line); border-radius: 8px; padding: 12px; margin: 0;
        white-space: pre-wrap; word-break: break-word; }
  .curl { font-size: 12px; }
  .status { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 10px; }
  .chip { font-weight: 700; padding: 2px 10px; border-radius: 999px; color: #fff; }
  .chip.ok { background: var(--ok); } .chip.warn { background: var(--warn); } .chip.err { background: var(--err); }
  .problem { border-left: 3px solid var(--err); padding: 8px 12px; margin-bottom: 10px; background: var(--code-bg); border-radius: 0 6px 6px 0; }
  .problem b { display: block; }
  .j-k { color: var(--k); } .j-s { color: var(--s); } .j-n { color: var(--n); } .j-b { color: var(--b); font-weight: 600; }
  .empty { color: var(--muted); margin-top: 40px; text-align: center; }
  .history { list-style: none; margin: 0; padding: 0; }
  .history li { display: flex; gap: 8px; align-items: center; padding: 4px 0; border-bottom: 1px dashed var(--line); cursor: pointer; font-size: 12.5px; }
  .history li:hover .mono { color: var(--accent); }
  .history .code { width: 34px; font-weight: 700; }
  @media (max-width: 960px) {
    .layout { grid-template-columns: 1fr; height: auto; }
    nav { max-height: 40vh; border-right: 0; border-bottom: 1px solid var(--line); }
    main { grid-template-columns: 1fr; overflow: visible; }
    section + section { border-left: 0; border-top: 1px solid var(--line); }
    header .base input { width: 140px; }
    header .base label { display: none; }
    header h1 { white-space: nowrap; }
  }
</style>
</head>
<body>
<header>
  <h1>📚 Bookstore API 콘솔</h1>
  <div class="base">
    <label for="base">Base URL</label>
    <input id="base" class="mono" placeholder="(같은 서버)">
  </div>
</header>
<div class="layout">
  <nav>
    <div class="filter"><input id="filter" placeholder="경로·설명 검색  ( / )"></div>
    <div id="nav"></div>
  </nav>
  <main>
    <section id="req"><p class="empty">왼쪽에서 API를 고르세요.</p></section>
    <section id="res"><p class="empty">응답이 여기에 표시됩니다.</p></section>
  </main>
</div>

<script>
/* ------------------------------------------------------------------
 * 엔드포인트 카탈로그 (커리큘럼 각 장의 컨트롤러와 1:1)
 *  - path 의 {id} 같은 부분은 경로 변수 입력칸이 된다. params 는 그 기본값.
 *  - query: 쿼리 파라미터. options 가 있으면 select 로 표시.
 *  - body : 요청 JSON 예시. 각 장 "실행 확인"과 시드 데이터(V2) 기준 값.
 *  새 컨트롤러를 만들면 여기에 한 줄 추가하면 된다.
 * ------------------------------------------------------------------ */
const CATALOG = [
  { chapter: "4장 · 저자", items: [
    { method: "GET", path: "/api/authors", label: "저자 목록 (이름 검색)",
      query: [{ name: "keyword", hint: "이름 부분 일치" }] },
    { method: "GET", path: "/api/authors/{id}", label: "저자 단건" },
    { method: "POST", path: "/api/authors", label: "저자 등록",
      body: { name: "정다은", birthYear: 1995 } },
    { method: "PUT", path: "/api/authors/{id}", label: "저자 전체 수정", params: { id: "6" },
      body: { name: "정다은", birthYear: 1996 } },
    { method: "PATCH", path: "/api/authors/{id}/name", label: "저자 이름 변경", params: { id: "6" },
      body: { name: "정다은(개정)" } },
    { method: "DELETE", path: "/api/authors/{id}", label: "저자 삭제" },
  ]},
  { chapter: "5장 · 도서 조회", items: [
    { method: "GET", path: "/api/books/{id}", label: "도서 요약" },
    { method: "GET", path: "/api/books", label: "카테고리별 도서",
      query: [{ name: "category", required: true, value: "프로그래밍", hint: "카테고리 이름" }] },
    { method: "GET", path: "/api/books/with-authors", label: "도서 + 저자 이름 목록" },
  ]},
  { chapter: "6장 · 도서 검색", items: [
    { method: "GET", path: "/api/books/search", label: "검색 (오프셋 페이지)",
      query: [
        { name: "keyword" }, { name: "categoryId" }, { name: "minPrice" }, { name: "maxPrice" },
        { name: "tag" }, { name: "language", hint: "ko, en …" },
        { name: "inStock", options: ["", "true", "false"] },
        { name: "sort", options: ["NEWEST", "PRICE_ASC", "PRICE_DESC", "TITLE"] },
        { name: "page", value: "0" }, { name: "size", value: "20" },
      ] },
    { method: "GET", path: "/api/books/scroll", label: "검색 (키셋 스크롤)",
      query: [
        { name: "keyword" }, { name: "categoryId" }, { name: "minPrice" }, { name: "maxPrice" },
        { name: "tag" }, { name: "language" }, { name: "inStock", options: ["", "true", "false"] },
        { name: "afterPrice", hint: "응답의 nextAfterPrice" }, { name: "afterId", hint: "응답의 nextAfterId" },
        { name: "size", value: "3" },
      ] },
  ]},
  { chapter: "7장 · 도서 상세", items: [
    { method: "GET", path: "/api/books/{id}/detail", label: "도서 상세 (MULTISET)", params: { id: "2" } },
  ]},
  { chapter: "8장 · 쓰기·주문", items: [
    { method: "POST", path: "/api/books", label: "도서 등록",
      body: { categoryId: 6, title: "테스트 주도 개발 입문", isbn: "9791100000080", price: 28000, stock: 5,
              publishedOn: "2026-11-01", tags: ["java", "testing"],
              attributes: { language: "ko", pages: 360 }, authorIds: [1, 2] } },
    { method: "PATCH", path: "/api/books/{id}/price", label: "가격 변경 (낙관적 락)",
      body: { price: 31000, version: 1 }, note: "version 은 도서 상세 응답의 version 값" },
    { method: "POST", path: "/api/orders", label: "주문 생성",
      body: { memberId: 1, items: [{ bookId: 3, quantity: 2 }, { bookId: 1, quantity: 1 }] } },
    { method: "GET", path: "/api/orders/{id}", label: "주문 상세" },
    { method: "GET", path: "/api/orders", label: "회원별 주문 목록",
      query: [{ name: "memberId", required: true, value: "1" }] },
    { method: "POST", path: "/api/orders/{id}/pay", label: "주문 결제", params: { id: "8" } },
    { method: "POST", path: "/api/orders/{id}/cancel", label: "주문 취소", params: { id: "8" } },
  ]},
  { chapter: "9장 · PostgreSQL 특화", items: [
    { method: "PUT", path: "/api/members", label: "회원 업서트 (이메일 기준)",
      body: { email: "new@example.com", name: "신규회원" } },
    { method: "GET", path: "/api/members", label: "회원 목록" },
    { method: "GET", path: "/api/members/{id}", label: "회원 단건" },
    { method: "POST", path: "/api/books/{id}/tags/{tag}", label: "태그 추가 (배열)", params: { tag: "Bestseller" } },
    { method: "DELETE", path: "/api/books/{id}/tags/{tag}", label: "태그 제거 (배열)", params: { tag: "bestseller" } },
    { method: "PATCH", path: "/api/books/{id}/attributes", label: "속성 병합 (JSONB ||)",
      body: { pages: 540 } },
    { method: "GET", path: "/api/tags/stats", label: "태그별 도서 수 (unnest)" },
  ]},
  { chapter: "10장 · 통계", items: [
    { method: "GET", path: "/api/stats/monthly-sales", label: "월별 매출 (윈도우 함수)" },
    { method: "GET", path: "/api/stats/best-sellers", label: "카테고리별 순위 (CTE + rank)",
      query: [{ name: "top", value: "1", hint: "1~50, 기본 3" }] },
    { method: "GET", path: "/api/stats/order-status", label: "상태별 주문 수 (enum)" },
    { method: "GET", path: "/api/categories/tree", label: "카테고리 트리 (재귀 CTE)" },
  ]},
];

/* ---------- 상태 ---------- */
const $ = (s) => document.querySelector(s);
const store = {
  get(k, d) { try { const v = localStorage.getItem("bsc." + k); return v == null ? d : JSON.parse(v); } catch { return d; } },
  set(k, v) { try { localStorage.setItem("bsc." + k, JSON.stringify(v)); } catch {} },
};
let current = null;
let hist = store.get("history", []);
$("#base").value = store.get("base", "");
$("#base").addEventListener("change", (e) => store.set("base", e.target.value.trim()));

const keyOf = (ep) => ep.method + " " + ep.path;
const flat = CATALOG.flatMap((g) => g.items.map((ep) => ({ ...ep, chapter: g.chapter })));
const esc = (s) => String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

/* ---------- 사이드바 ---------- */
function renderNav() {
  const q = $("#filter").value.trim().toLowerCase();
  $("#nav").innerHTML = CATALOG.map((g) => {
    const items = g.items.filter((ep) => !q || (ep.path + ep.label + ep.method).toLowerCase().includes(q));
    if (!items.length) return "";
    return `<h2>${esc(g.chapter)}</h2>` + items.map((ep) => `
      <button class="ep ${current && keyOf(current) === keyOf(ep) ? "active" : ""}" data-key="${esc(keyOf(ep))}">
        <span class="m ${ep.method}">${ep.method}</span>
        <span style="min-width:0"><span class="path mono">${esc(ep.path)}</span><span class="label">${esc(ep.label)}</span></span>
      </button>`).join("");
  }).join("");
}
$("#nav").addEventListener("click", (e) => {
  const b = e.target.closest("button.ep"); if (!b) return;
  select(flat.find((ep) => keyOf(ep) === b.dataset.key));
});
$("#filter").addEventListener("input", renderNav);
document.addEventListener("keydown", (e) => {
  if (e.key === "/" && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) { e.preventDefault(); $("#filter").focus(); }
});

/* ---------- 요청 패널 ---------- */
const pathVars = (p) => [...p.matchAll(/\{(\w+)\}/g)].map((m) => m[1]);
const saved = (ep) => store.get("form." + keyOf(ep), null);

function select(ep, prefill) {
  current = ep;
  renderNav();
  const s = prefill || saved(ep) || {};
  const vars = pathVars(ep.path);
  $("#req").innerHTML = `
    <div class="title"><span class="m ${ep.method}">${ep.method}</span><code>${esc(ep.path)}</code></div>
    <p class="desc">${esc(ep.label)} · ${esc(ep.chapter)}${ep.note ? " — " + esc(ep.note) : ""}</p>
    ${vars.length ? `<h3>경로 변수</h3>` + vars.map((v) => field("p", v, s.p?.[v] ?? ep.params?.[v] ?? "1", { required: true })).join("") : ""}
    ${ep.query ? `<h3>쿼리 파라미터</h3>` + ep.query.map((q) => field("q", q.name, s.q?.[q.name] ?? q.value ?? "", q)).join("") : ""}
    ${ep.body ? `<h3>요청 본문 (JSON)</h3><textarea id="body" spellcheck="false">${esc(s.body ?? JSON.stringify(ep.body, null, 2))}</textarea>` : ""}
    <div class="actions">
      <button class="primary" id="send">보내기</button>
      <button class="ghost" id="reset">초기화</button>
      <span class="hint">Ctrl/⌘ + Enter</span>
    </div>
    <h3>미리보기</h3>
    <pre class="curl mono" id="curl"></pre>
    <h3>최근 요청</h3>
    <ul class="history" id="history"></ul>`;
  $("#req").oninput = () => { persist(); updateCurl(); };
  $("#send").onclick = send;
  $("#reset").onclick = () => { store.set("form." + keyOf(ep), null); select(ep, {}); };
  updateCurl();
  renderHistory();
}

function field(kind, name, value, opt = {}) {
  const id = `${kind}-${name}`;
  const label = `<label for="${id}">${esc(name)}${opt.required ? " *" : ""}${opt.hint ? `<small>${esc(opt.hint)}</small>` : ""}</label>`;
  const input = opt.options
    ? `<select id="${id}" data-kind="${kind}" data-name="${esc(name)}">${opt.options.map((o) =>
        `<option value="${esc(o)}" ${o === value ? "selected" : ""}>${o === "" ? "(지정 안 함)" : esc(o)}</option>`).join("")}</select>`
    : `<input id="${id}" class="mono" data-kind="${kind}" data-name="${esc(name)}" value="${esc(value)}">`;
  return `<div class="field">${label}${input}</div>`;
}

function readForm() {
  const f = { p: {}, q: {} };
  document.querySelectorAll("#req [data-kind]").forEach((el) => { f[el.dataset.kind][el.dataset.name] = el.value; });
  const b = $("#body"); if (b) f.body = b.value;
  return f;
}
function persist() { if (current) store.set("form." + keyOf(current), readForm()); }

function buildUrl(ep, f) {
  let path = ep.path.replace(/\{(\w+)\}/g, (_, v) => encodeURIComponent(f.p[v] ?? ""));
  const qs = new URLSearchParams();
  Object.entries(f.q).forEach(([k, v]) => { if (v !== "") qs.append(k, v); });
  const s = qs.toString();
  return ($("#base").value.trim().replace(/\/$/, "")) + path + (s ? "?" + s : "");
}

function updateCurl() {
  const f = readForm();
  const url = buildUrl(current, f);
  const abs = /^https?:/.test(url) ? url : location.origin + url;
  let c = `curl -i -X ${current.method} '${abs}'`;
  if (f.body != null) {
    let body = f.body;
    try { body = JSON.stringify(JSON.parse(f.body)); } catch {}
    c += ` \\\n  -H 'Content-Type: application/json' \\\n  -d '${body.replace(/'/g, "'\\''")}'`;
  }
  $("#curl").textContent = c;
}

/* ---------- 전송 ---------- */
async function send() {
  const ep = current, f = readForm();
  const url = buildUrl(ep, f);
  const init = { method: ep.method, headers: { Accept: "application/json" } };
  if (f.body != null) {
    try { JSON.parse(f.body); } catch (e) { return showLocalError("요청 본문이 올바른 JSON이 아닙니다", e.message); }
    init.headers["Content-Type"] = "application/json";
    init.body = f.body;
  }
  const btn = $("#send"); btn.disabled = true; btn.textContent = "보내는 중…";
  const t0 = performance.now();
  try {
    const res = await fetch(url, init);
    const ms = Math.round(performance.now() - t0);
    const text = await res.text();
    let json = null; try { json = text ? JSON.parse(text) : null; } catch {}
    showResponse(ep, res, ms, text, json);
    hist.unshift({ key: keyOf(ep), url, status: res.status, form: f, at: Date.now() });
    hist = hist.slice(0, 30); store.set("history", hist); renderHistory();
  } catch (e) {
    showLocalError("서버에 연결하지 못했습니다",
      `${e.message}\n\n애플리케이션이 실행 중인지 확인하세요 (./gradlew bootRun).\nBase URL을 다른 서버로 바꿨다면 그 서버에 CORS 설정이 필요합니다.`);
  } finally { btn.disabled = false; btn.textContent = "보내기"; }
}

function showResponse(ep, res, ms, text, json) {
  const cls = res.status < 300 ? "ok" : res.status < 500 ? "warn" : "err";
  const loc = res.headers.get("Location");
  const isProblem = (res.headers.get("Content-Type") || "").includes("problem+json") || (json && json.title && json.status);
  let extra = "";
  if (ep.path === "/api/books/scroll" && json && json.hasNext) {
    extra = `<button class="ghost" id="nextScroll">다음 페이지 이어서 →</button>`;
  }
  $("#res").innerHTML = `
    <div class="status">
      <span class="chip ${cls}">${res.status} ${esc(res.statusText || "")}</span>
      <span class="hint">${ms} ms · ${new Blob([text]).size.toLocaleString()} bytes</span>
      ${Array.isArray(json) ? `<span class="hint">· ${json.length}건</span>` : ""}
      ${json && json.totalElements != null ? `<span class="hint">· 전체 ${json.totalElements}건 (${json.page + 1}/${json.totalPages || 1} 페이지)</span>` : ""}
      ${extra}
    </div>
    ${loc ? `<p class="hint">Location: <code>${esc(loc)}</code></p>` : ""}
    ${isProblem && res.status >= 400 ? `<div class="problem"><b>${esc(json.title || "오류")}</b>${esc(json.detail || "")}</div>` : ""}
    ${text ? `<pre class="mono">${json !== null ? highlight(json) : esc(text)}</pre>` : `<p class="hint">(본문 없음)</p>`}`;
  const n = $("#nextScroll");
  if (n) n.onclick = () => {
    const f = readForm();
    f.q.afterPrice = String(json.nextAfterPrice); f.q.afterId = String(json.nextAfterId);
    store.set("form." + keyOf(ep), f); select(ep, f); send();
  };
}

function showLocalError(title, detail) {
  $("#res").innerHTML = `<div class="problem"><b>${esc(title)}</b><span style="white-space:pre-wrap">${esc(detail)}</span></div>`;
}

function highlight(obj) {
  return esc(JSON.stringify(obj, null, 2)).replace(
    /(&quot;(?:[^&]|&(?!quot;))*?&quot;)(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/g,
    (m, str, colon, lit) => str ? `<span class="${colon ? "j-k" : "j-s"}">${str}</span>${colon || ""}`
      : lit ? `<span class="j-b">${m}</span>` : `<span class="j-n">${m}</span>`);
}

/* ---------- 히스토리 ---------- */
function renderHistory() {
  const el = $("#history"); if (!el) return;
  if (!hist.length) { el.innerHTML = `<li class="hint" style="cursor:default">아직 없음</li>`; return; }
  el.innerHTML = hist.slice(0, 12).map((h, i) => {
    const [m] = h.key.split(" ");
    const c = h.status < 300 ? "var(--ok)" : h.status < 500 ? "var(--warn)" : "var(--err)";
    return `<li data-i="${i}"><span class="m ${m}">${m}</span><span class="code" style="color:${c}">${h.status}</span><span class="mono">${esc(h.url)}</span></li>`;
  }).join("");
  el.onclick = (e) => {
    const li = e.target.closest("li[data-i]"); if (!li) return;
    const h = hist[+li.dataset.i]; const ep = flat.find((x) => keyOf(x) === h.key);
    if (ep) { store.set("form." + h.key, h.form); select(ep, h.form); }
  };
}

document.addEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "Enter" && current) { e.preventDefault(); send(); }
});

renderNav();
select(flat.find((ep) => keyOf(ep) === store.get("last", "GET /api/authors")) || flat[0]);
const _select = select;
select = (ep, p) => { store.set("last", keyOf(ep)); _select(ep, p); };
</script>
</body>
</html>
```

### 실행 확인

```bash
docker compose up -d          # PostgreSQL 18 기동
docker compose ps             # STATUS 가 healthy 인지 확인
./gradlew compileJava         # 빌드 설정에 오류가 없는지 확인
```

아직 마이그레이션과 생성 코드가 없으므로 `bootRun` 은 3장 이후에 실행합니다. 1-8의 API 콘솔도 그때부터 열립니다.

### 연습 과제

1. `./gradlew dependencyInsight --dependency org.jooq:jooq` 를 실행해 실제 선택된 jOOQ 버전과 "왜 그 버전이 선택되었는지"를 확인해 보세요.
2. `./gradlew tasks --all` 의 출력에서 jOOQ 플러그인이 등록한 `jooqCodegen` 태스크를 찾아보세요.

**예상 결과**

1. `org.jooq:jooq:3.21.9`가 선택됩니다. 이유 줄에는 "Selected by rule" 같은 표시가 붙습니다. dependency-management 플러그인이 `extra["jooq.version"]` 값으로 BOM 버전을 덮어썼기 때문입니다. 그 줄을 지우고 다시 실행하면 Spring Boot BOM이 정한 버전이 나옵니다.
2. 목록에 `jooqCodegen`이 보입니다. `jooq { configuration { … } }` 설정을 읽어 코드를 생성하는 태스크이고, 2장부터 쓰는 `-x jooqCodegen`이 제외하는 대상이 이 태스크입니다.

## 2장. Flyway로 스키마 설계와 마이그레이션

jOOQ는 "스키마가 진실의 원천(Source of Truth)"인 접근이므로, 코드를 쓰기 전에 Flyway 마이그레이션으로 스키마부터 확정합니다. 이 장에서는 스키마(V1), 시드 데이터(V2), 인덱스(V3) 세 개의 마이그레이션을 만듭니다.

### 학습 목표

- Flyway의 버전 마이그레이션 규칙과 `flyway_schema_history` 테이블의 역할을 이해한다.
- jOOQ 코드 생성에 유리한 스키마 설계 습관(명확한 FK, enum 타입, NOT NULL)을 익힌다.

### 2-1. Flyway 핵심 개념

- **파일 이름 규칙**: `V<버전>__<설명>.sql` (밑줄 두 개). 버전은 숫자 순서대로 한 번만 적용됩니다. `R__<설명>.sql` 은 내용이 바뀔 때마다 다시 적용되는 반복 마이그레이션입니다(뷰·함수에 적합).
- **`flyway_schema_history`**: 어떤 버전이 언제, 어떤 체크섬으로 적용되었는지 기록하는 테이블입니다. 1장에서 이 테이블을 코드 생성 대상에서 제외했습니다.
- **적용된 마이그레이션은 절대 수정하지 않기**: 수정하면 체크섬이 달라져 `validate-on-migrate` 에서 기동이 실패합니다. 변경은 항상 새 버전(V4, V5…)으로 추가합니다.
- **Flyway는 앱이 실행한다**: 마이그레이션은 애플리케이션이 기동할 때 Spring Boot 자동 설정이 적용합니다. 로컬 도커 DB도, 12장의 테스트 컨테이너도 같은 `db/migration` 디렉터리를 읽으므로 스키마가 항상 일치합니다.

### 2-2. 스키마 설계 포인트

| 테이블 | 역할 | jOOQ 학습 포인트 |
| --- | --- | --- |
| `category` | 카테고리(자기 참조 계층) | 재귀 CTE (10장) |
| `author` | 저자 | 기본 CRUD (4장) |
| `book` | 도서. `tags TEXT[]`, `attributes JSONB`, `version` | 배열·JSONB·낙관적 락 (6, 8, 9장) |
| `book_author` | 도서-저자 N:M | MULTISET (7장) |
| `member` | 회원. `email` UNIQUE | `ON CONFLICT` 업서트 (9장) |
| `orders` | 주문. `status order_status` | PostgreSQL enum → Java enum (8장) |
| `order_item` | 주문 항목 | 집계·윈도우 함수 (10장) |

- PK는 `BIGINT GENERATED ALWAYS AS IDENTITY` 를 씁니다. SQL 표준 방식이며 jOOQ는 이를 identity 컬럼으로 인식합니다.
- `order` 는 SQL 예약어이므로 테이블 이름을 `orders` 로 합니다. jOOQ 생성 클래스는 `Orders`, 레코드는 `OrdersRecord` 가 됩니다.
- FK를 빠짐없이 선언해야 jOOQ가 path 조인 메서드(`BOOK.category()` 등)를 만들어 줍니다.
- 금액은 부동소수점이 아닌 `NUMERIC` 을 쓰고, jOOQ는 이를 `BigDecimal` 로 생성합니다.

### 2-3. `src/main/resources/db/migration/V1__init_schema.sql`

```sql
-- 주문 상태: PostgreSQL enum 타입 → jOOQ가 Java enum(OrderStatus)으로 생성한다
CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'SHIPPED', 'CANCELLED');

CREATE TABLE category (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES category (id),
    name      VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE author (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    birth_year INTEGER,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE book (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id  BIGINT         NOT NULL REFERENCES category (id),
    title        VARCHAR(200)   NOT NULL,
    isbn         VARCHAR(13)    NOT NULL UNIQUE,
    price        NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock        INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    published_on DATE,
    tags         TEXT[]         NOT NULL DEFAULT '{}',
    attributes   JSONB          NOT NULL DEFAULT '{}'::jsonb,
    version      INTEGER        NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE book_author (
    book_id   BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES author (id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE member (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id    BIGINT         NOT NULL REFERENCES member (id),
    status       order_status   NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ordered_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE order_item (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    book_id    BIGINT         NOT NULL REFERENCES book (id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10, 2) NOT NULL,
    UNIQUE (order_id, book_id)
);
```

### 2-4. `src/main/resources/db/migration/V2__seed_data.sql`

시드 데이터는 모든 예제와 테스트(12장)의 기준이 됩니다. identity PK는 직접 지정하지 않고, 이름·ISBN·이메일 같은 자연 키로 참조해 어떤 환경에서도 동일하게 들어가도록 작성했습니다.

```sql
-- 카테고리 (3단 계층)
INSERT INTO category (name, parent_id) VALUES ('국내도서', NULL), ('외국도서', NULL);
INSERT INTO category (name, parent_id) VALUES
    ('소설',    (SELECT id FROM category WHERE name = '국내도서')),
    ('IT',      (SELECT id FROM category WHERE name = '국내도서')),
    ('Fiction', (SELECT id FROM category WHERE name = '외국도서'));
INSERT INTO category (name, parent_id) VALUES
    ('프로그래밍',   (SELECT id FROM category WHERE name = 'IT')),
    ('데이터베이스', (SELECT id FROM category WHERE name = 'IT'));

-- 저자
INSERT INTO author (name, birth_year) VALUES
    ('김지훈', 1982),
    ('이서연', 1990),
    ('박민준', 1975),
    ('최유진', 1988),
    ('Emily Carter', 1979);

-- 도서
INSERT INTO book (category_id, title, isbn, price, stock, published_on, tags, attributes) VALUES
    ((SELECT id FROM category WHERE name = '프로그래밍'), '모던 자바 실전', '9791100000011', 32000, 50, '2025-03-10',
     '{java,backend}', '{"language": "ko", "pages": 520, "format": "PAPERBACK"}'),
    ((SELECT id FROM category WHERE name = '프로그래밍'), '스프링 부트 4 완벽 가이드', '9791100000028', 38000, 30, '2026-01-15',
     '{java,spring,backend}', '{"language": "ko", "pages": 640, "format": "PAPERBACK"}'),
    ((SELECT id FROM category WHERE name = '데이터베이스'), 'PostgreSQL 운영과 튜닝', '9791100000035', 35000, 20, '2025-08-01',
     '{database,postgresql}', '{"language": "ko", "pages": 480, "format": "PAPERBACK"}'),
    ((SELECT id FROM category WHERE name = '데이터베이스'), 'SQL 첫걸음 다시 보기', '9791100000042', 22000, 100, '2024-11-20',
     '{database,sql,beginner}', '{"language": "ko", "pages": 300, "format": "EBOOK"}'),
    ((SELECT id FROM category WHERE name = '소설'), '새벽의 정원', '9791100000059', 15000, 70, '2025-05-05',
     '{novel,bestseller}', '{"language": "ko", "pages": 280, "format": "PAPERBACK"}'),
    ((SELECT id FROM category WHERE name = '소설'), '바다로 가는 기차', '9791100000066', 14500, 0, '2023-09-09',
     '{novel}', '{"language": "ko", "pages": 250, "format": "PAPERBACK"}'),
    ((SELECT id FROM category WHERE name = 'Fiction'), 'The Quiet Algorithm', '9781000000017', 27000, 15, '2025-10-01',
     '{novel,english}', '{"language": "en", "pages": 350, "format": "HARDCOVER"}'),
    ((SELECT id FROM category WHERE name = '프로그래밍'), 'jOOQ로 배우는 타입 안전 SQL', '9791100000073', 30000, 40, '2026-06-30',
     '{java,jooq,database}', '{"language": "ko", "pages": 420, "format": "PAPERBACK"}');

-- 도서-저자 연결 (공저 포함)
INSERT INTO book_author (book_id, author_id)
SELECT b.id, a.id
FROM (VALUES
          ('9791100000011', '김지훈'),
          ('9791100000028', '김지훈'),
          ('9791100000028', '이서연'),
          ('9791100000035', '박민준'),
          ('9791100000042', '박민준'),
          ('9791100000059', '최유진'),
          ('9791100000066', '최유진'),
          ('9781000000017', 'Emily Carter'),
          ('9791100000073', '이서연'),
          ('9791100000073', '박민준')) AS v(isbn, author_name)
JOIN book b ON b.isbn = v.isbn
JOIN author a ON a.name = v.author_name;

-- 회원
INSERT INTO member (email, name) VALUES
    ('minsu@example.com', '김민수'),
    ('jiyoung@example.com', '이지영'),
    ('hana@example.com', '박하나');

-- 주문: 데이터 변경 CTE(INSERT ... RETURNING)로 주문과 항목을 한 번에 넣는다
WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'minsu@example.com'), 'PAID', 70000, '2026-07-03 10:00:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000011', 1), ('9791100000028', 1)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'SHIPPED', 30000, '2026-07-15 14:30:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000059', 2)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'hana@example.com'), 'PAID', 57000, '2026-08-02 09:15:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000035', 1), ('9791100000042', 1)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'minsu@example.com'), 'SHIPPED', 60000, '2026-08-20 20:00:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000073', 2)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'CANCELLED', 27000, '2026-09-05 11:00:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9781000000017', 1)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'hana@example.com'), 'PENDING', 53000, '2026-09-28 18:45:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000059', 1), ('9791100000028', 1)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;

WITH o AS (
    INSERT INTO orders (member_id, status, total_amount, ordered_at)
    VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'PAID', 62000, '2026-10-10 13:20:00+09')
    RETURNING id)
INSERT INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o, (VALUES ('9791100000073', 1), ('9791100000011', 1)) AS v(isbn, qty)
JOIN book b ON b.isbn = v.isbn;
```

시드 주문의 매출 인정 기준(PAID·SHIPPED) 합계는 7월 100,000원, 8월 117,000원, 10월 62,000원입니다. 9월 주문은 취소·대기 상태라 제외됩니다. 10장과 12장에서 이 숫자를 검증합니다.

> 실무 팁: 운영 DB에 들어가면 안 되는 시드 데이터는 별도 location(예: `classpath:db/seed`)으로 분리하고 프로파일별로 `spring.flyway.locations` 를 다르게 줍니다. 이 커리큘럼에서는 단순함을 위해 한 디렉터리에 둡니다.

### 2-5. `src/main/resources/db/migration/V3__add_indexes.sql`

스키마가 진화하는 모습을 보여 주기 위해 인덱스는 별도 버전으로 추가합니다. PostgreSQL은 FK 컬럼에 자동으로 인덱스를 만들지 않는다는 점에 주의하세요.

```sql
-- FK 조인·필터용
CREATE INDEX idx_category_parent_id ON category (parent_id);
CREATE INDEX idx_book_category_id ON book (category_id);
CREATE INDEX idx_book_author_author_id ON book_author (author_id);
CREATE INDEX idx_order_item_book_id ON order_item (book_id);

-- 회원별 최근 주문 조회용
CREATE INDEX idx_orders_member_id_ordered_at ON orders (member_id, ordered_at DESC);

-- 키셋 페이지네이션(가격순) 용 복합 인덱스 (6장)
CREATE INDEX idx_book_price_id ON book (price, id);

-- 배열 포함 연산자(@>) 용 GIN 인덱스 (6장)
CREATE INDEX idx_book_tags ON book USING GIN (tags);
```

### 실행 확인

```bash
./gradlew bootRun -x jooqCodegen
# ... Migrating schema "public" to version "1 - init schema"
# ... Migrating schema "public" to version "3 - add indexes"
# Started BookstoreApplication 이 보이면 Ctrl+C 로 종료

docker exec -it bookstore-postgres psql -U bookstore -d bookstore \
  -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

`-x jooqCodegen` 은 "이번 빌드에서 코드 생성은 빼라"는 뜻입니다. 스키마가 비어 있는 DB로 코드 생성이 먼저 돌아 생성 코드가 비워지는 일을 막아 줍니다. 플러그인이 코드 생성을 빌드에 자동으로 끼워 넣지 않는 경우에도 붙여서 해될 것은 없습니다.

실수로 적용된 마이그레이션을 고쳐 버렸다면, 로컬에서는 `docker compose down -v` 로 볼륨을 지우고 처음부터 다시 적용하는 것이 가장 깔끔합니다.

### 연습 과제

1. V2 파일에 공백 한 줄을 추가하고 `./gradlew bootRun -x jooqCodegen` 을 다시 실행해 체크섬 오류(Validate failed) 메시지를 직접 확인한 뒤 원복하세요.
2. `\d book` 으로 컬럼 타입과 인덱스를 확인하고, 각 인덱스가 어느 장의 쿼리를 위한 것인지 추측해 보세요.

**예상 결과**

1. 앱이 기동하지 않고 Flyway가 `Validate failed: Migrations have failed validation`과 함께 "Migration checksum mismatch for migration version 2"를 출력합니다. DB(`flyway_schema_history`)에 기록된 체크섬과 로컬 파일의 체크섬이 다르다는 뜻입니다. 공백 하나도 내용 변경으로 보므로, 적용된 마이그레이션은 고치지 않는다는 규칙을 확인하는 과제입니다. 원복하면 다시 기동합니다.
2. `tags`는 `text[]`, `attributes`는 `jsonb`, `created_at`은 `timestamp with time zone`으로 보입니다. 인덱스와 쓰임새는 이렇습니다.
   - `book_isbn_key` (UNIQUE): 8장 중복 ISBN → 409
   - `idx_book_category_id`: 5장 카테고리 조인·필터
   - `idx_book_price_id` (price, id): 6장 키셋 페이지네이션
   - `idx_book_tags` (GIN): 6장 태그 검색(`@>`)
   - CHECK `price >= 0`, `stock >= 0`: 8장 재고 차감의 마지막 방어선

## 3장. jOOQ 코드 생성 설정과 생성물 이해

이 장에서 `./gradlew jooqCodegen` 한 번으로 스키마 전체가 타입 안전한 Java 코드가 됩니다. 이후 모든 쿼리는 이 생성 코드(`BOOK`, `BOOK.TITLE` 등)를 사용하므로, 무엇이 어떻게 만들어지는지 이해하는 것이 이 장의 핵심입니다.

### 학습 목표

- 생성되는 클래스(Table, TableField, Record, Keys, enum, path)의 역할을 설명할 수 있다.
- `forcedType` 과 `Converter` 로 DB 타입을 원하는 Java 타입에 매핑한다.
- 스키마 변경 → 재생성 → 컴파일 에러로 영향 범위를 찾는 워크플로를 익힌다.

### 3-1. 코드 생성 전에 필요한 클래스: JSONB 변환기

1장의 `forcedType` 설정은 `book.attributes`(JSONB) 컬럼을 `BookAttributes` record로 다루겠다는 선언입니다. 코드 생성기는 이 클래스 이름을 문자열로만 참조하지만, 생성된 코드를 컴파일하려면 두 클래스가 미리 있어야 합니다.

`src/main/java/com/example/bookstore/book/BookAttributes.java`

```java
package com.example.bookstore.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * book.attributes(JSONB) 컬럼에 대응하는 값 객체.
 * null 인 속성은 JSON에서 생략한다 (9장의 JSONB 병합 연산에서 중요).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookAttributes(
        String language,
        Integer pages,
        String format
) {

    public static BookAttributes empty() {
        return new BookAttributes(null, null, null);
    }
}
```

`src/main/java/com/example/bookstore/book/BookAttributesConverter.java`

```java
package com.example.bookstore.book;

import org.jooq.Converter;
import org.jooq.JSONB;
import tools.jackson.databind.json.JsonMapper;

/**
 * jOOQ Converter: DB 타입(JSONB) <-> 사용자 타입(BookAttributes).
 * 생성 코드가 new BookAttributesConverter() 로 인스턴스를 만들기 때문에
 * public 기본 생성자가 반드시 있어야 한다.
 */
public class BookAttributesConverter implements Converter<JSONB, BookAttributes> {

    // Spring Boot 4는 Jackson 3(패키지 tools.jackson)을 사용한다. JsonMapper는 스레드 안전하다.
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Override
    public BookAttributes from(JSONB databaseObject) {
        if (databaseObject == null) {
            return null;
        }
        return MAPPER.readValue(databaseObject.data(), BookAttributes.class);
    }

    @Override
    public JSONB to(BookAttributes userObject) {
        if (userObject == null) {
            return null;
        }
        return JSONB.jsonb(MAPPER.writeValueAsString(userObject));
    }

    @Override
    public Class<JSONB> fromType() {
        return JSONB.class;
    }

    @Override
    public Class<BookAttributes> toType() {
        return BookAttributes.class;
    }
}
```

- `Converter<T, U>` 의 `T` 는 DB 쪽 타입(jOOQ가 기본으로 쓰는 `org.jooq.JSONB`), `U` 는 애플리케이션이 다루고 싶은 타입입니다. `from` 은 읽을 때, `to` 는 쓸 때 호출됩니다.
- Jackson 3에서는 예외가 비검사 예외(`JacksonException`)로 바뀌어 `try-catch` 없이 작성할 수 있습니다. 어노테이션(`@JsonInclude` 등)은 기존 `com.fasterxml.jackson.annotation` 패키지를 그대로 씁니다.

### 3-2. 코드 생성 실행

```bash
docker compose up -d
./gradlew bootRun -x jooqCodegen   # 마이그레이션 적용 후 Ctrl+C (2장에서 이미 했다면 생략)
./gradlew jooqCodegen              # DB 스키마를 읽어 src/generated/java 에 코드 생성
./gradlew compileJava              # 생성 코드가 컴파일되는지 확인
```

코드 생성기는 스키마를 "마이그레이션 파일"이 아니라 "실제 DB"에서 읽습니다. 그래서 항상 ① 앱으로 마이그레이션 적용, ② 코드 생성 순서를 지킵니다.

생성 결과는 대략 다음 구조입니다(jOOQ 버전에 따라 보조 클래스가 조금 다를 수 있습니다).

```text
src/generated/java/com/example/bookstore/jooq/
├── DefaultCatalog.java      # 카탈로그(최상위)
├── Public.java              # public 스키마
├── Tables.java              # 모든 테이블 상수(AUTHOR, BOOK, ...)의 진입점
├── Keys.java                # PK, UNIQUE, FK 제약 정보
├── Indexes.java             # 인덱스 정보
├── enums/
│   └── OrderStatus.java     # CREATE TYPE order_status → Java enum
└── tables/
    ├── Author.java, Book.java, BookAuthor.java, Category.java,
    │   Member.java, Orders.java, OrderItem.java   # 테이블 클래스
    └── records/
        └── AuthorRecord.java, BookRecord.java, ... # 행(row) 클래스
```

### 3-3. 생성물 해부

생성된 `Book.java` 의 핵심 부분만 발췌하면 다음과 같습니다(직접 작성하는 파일이 아니라 생성기가 만드는 파일입니다).

```java
// (발췌) 생성 코드: com.example.bookstore.jooq.tables.Book
public class Book extends TableImpl<BookRecord> {
    public static final Book BOOK = new Book();

    public final TableField<BookRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");
    public final TableField<BookRecord, String> TITLE = createField(DSL.name("title"), SQLDataType.VARCHAR(200).nullable(false), this, "");
    public final TableField<BookRecord, BigDecimal> PRICE = createField(DSL.name("price"), SQLDataType.NUMERIC(10, 2).nullable(false), this, "");
    public final TableField<BookRecord, String[]> TAGS = createField(DSL.name("tags"), SQLDataType.CLOB.array()..., this, "");
    public final TableField<BookRecord, BookAttributes> ATTRIBUTES = createField(DSL.name("attributes"), SQLDataType.JSONB..., this, "", new BookAttributesConverter());
    // ...

    // FK book.category_id → category.id 로부터 만들어진 path (5장의 암시적 조인)
    public CategoryPath category() { ... }
}
```

| 생성물 | 예 | 의미 |
| --- | --- | --- |
| 테이블 상수 | `Tables.BOOK` | FROM 절에 쓰는 테이블. `BOOK.as("b")` 로 별칭도 만들 수 있음 |
| `TableField<R, T>` | `BOOK.PRICE` (`T`=`BigDecimal`) | 컬럼. `T` 덕분에 `BOOK.PRICE.eq("abc")` 같은 실수가 컴파일 에러 |
| 레코드 | `BookRecord` | 한 행. getter/setter와 `store()`, `delete()` 같은 액티브 레코드 기능 제공 |
| enum | `OrderStatus.PAID` | PostgreSQL enum이 Java enum으로. 오타가 컴파일 에러 |
| path | `BOOK.category()` | FK를 따라가는 암시적 조인 |
| Keys | `Keys.BOOK_PKEY` | 제약 조건 메타데이터. `UpdatableRecord` 가 PK를 알기 위해 사용 |

PostgreSQL 타입과 Java 타입의 기본 대응은 다음과 같습니다.

| PostgreSQL | Java (생성 코드) |
| --- | --- |
| `BIGINT` | `Long` |
| `INTEGER` | `Integer` |
| `NUMERIC(p, s)` | `BigDecimal` |
| `VARCHAR`, `TEXT` | `String` |
| `DATE` | `LocalDate` |
| `TIMESTAMPTZ` | `OffsetDateTime` |
| `TEXT[]` | `String[]` |
| `JSONB` | `org.jooq.JSONB` (이 프로젝트에서는 `forcedType` 으로 `BookAttributes`) |
| `order_status` (enum) | `OrderStatus` |

### 3-4. forcedType 더 알아보기

`forcedType` 은 "어떤 컬럼을(`includeExpression` 또는 `includeTypes`) 어떤 Java 타입으로(`userType`) 어떻게 변환할지(`converter`)"를 정하는 규칙입니다.

- `includeExpression` 은 `스키마.테이블.컬럼` 에 대한 정규식입니다. `.*\.created_at` 처럼 여러 컬럼에 한 번에 적용할 수도 있습니다.
- `includeTypes` 는 DB 타입 이름에 대한 정규식입니다. 예: `includeTypes = "TIMESTAMPTZ"` + `userType = "java.time.Instant"`.
- 변환기를 코드 생성에 넣으면 모든 쿼리에서 자동 적용됩니다. 특정 쿼리에서만 변환이 필요하면 5장의 `Field.convertFrom()`(애드혹 변환기)을 씁니다.

### 3-5. 스키마 변경 워크플로

1. 새 마이그레이션 `V4__xxx.sql` 작성
2. `./gradlew bootRun -x jooqCodegen` 으로 로컬 DB에 V4를 적용한 뒤 종료
3. `./gradlew jooqCodegen` 으로 코드 재생성
4. `./gradlew compileJava` → 컬럼 삭제·타입 변경의 영향을 받는 코드가 컴파일 에러로 나타남
5. 마이그레이션 + 생성 코드 + 애플리케이션 코드를 **한 커밋**으로 올림

이것이 jOOQ의 가장 큰 가치입니다. JPA나 MyBatis에서는 운영 중 런타임 에러로 발견되던 문제를 빌드 단계에서 잡습니다.

### 실행 확인

```bash
./gradlew bootRun
# ... Successfully validated 3 migrations
# ... Schema "public" is up to date. No migration necessary.
# ... Started BookstoreApplication
```

아직 컨트롤러는 없지만, Flyway 검증과 jOOQ `DSLContext` 빈 생성까지 성공하면 성공입니다.

### 연습 과제

1. 생성된 `OrderStatus.java` 를 열어 enum이 어떤 인터페이스(`EnumType`)를 구현하는지 확인해 보세요.
2. 연습용으로 `V4__add_author_country.sql`(예: `ALTER TABLE author ADD COLUMN country VARCHAR(2);`)를 만들고 재생성해 `AUTHOR.COUNTRY` 가 생기는지 확인한 뒤, 파일을 지우고 `docker compose down -v` 로 원복하세요(이후 장은 V3까지를 가정합니다).

**예상 결과**

1. `OrderStatus`는 `org.jooq.EnumType`을 구현합니다. `getLiteral()`이 DB에 저장되는 값(`"PENDING"` 등)을, `getName()`과 `getSchema()`가 PostgreSQL enum 타입 `order_status`의 정보를 돌려줍니다. 그래서 Java enum과 DB enum이 문자열 변환 없이 타입 안전하게 오갑니다.
2. 재생성하면 `AUTHOR.COUNTRY`(`TableField<AuthorRecord, String>`)와 `AuthorRecord.getCountry()`/`setCountry()`가 생기고, `git diff`에 생성 코드 변경이 보입니다. 원복할 때는 파일 삭제와 `docker compose down -v` 뒤 V3까지 다시 적용하고 `./gradlew jooqCodegen`까지 다시 돌려야 합니다. 생성 코드에 `COUNTRY`가 남아 있으면 `selectFrom(AUTHOR)`가 없는 컬럼을 조회해 SQL 오류가 납니다.

## 4장. DSLContext 기초 CRUD — 저자 API

첫 기능으로 저자(author) CRUD API를 만듭니다. SELECT·INSERT·UPDATE·DELETE의 기본형, 결과를 꺼내는 `fetch` 계열 메서드, 그리고 프로젝트 전체에서 쓸 예외 처리 구조를 이 장에서 정합니다.

### 학습 목표

- Spring이 주입하는 `DSLContext` 로 쿼리를 만들고 실행한다.
- `fetch`, `fetchOne`, `fetchOptional`, `fetchSingle` 의 차이를 구분한다.
- "DSL로 UPDATE"와 "액티브 레코드(`store()`)로 UPDATE" 두 방식을 비교한다.

### 4-1. 개념: DSLContext와 쿼리의 흐름

`DSLContext` 는 jOOQ의 진입점입니다. Spring Boot의 jOOQ 자동 설정이 `DataSource`, SQL 방언, 트랜잭션 연동, 예외 변환을 모두 구성한 `DSLContext` 빈을 만들어 주므로 생성자 주입만 받으면 됩니다.

jOOQ 쿼리는 SQL 절의 순서대로 메서드를 잇습니다. 각 단계가 다음에 올 수 있는 메서드만 노출하는 타입(`SelectWhereStep`, `SelectConditionStep` …)을 반환하므로, IDE 자동완성이 곧 SQL 문법 가이드가 됩니다.

```java
// SELECT * FROM author WHERE id = ? 에 해당
dsl.selectFrom(AUTHOR)        // SELECT ... FROM
   .where(AUTHOR.ID.eq(id))   // WHERE
   .fetchOptional();          // 실행 + 결과 꺼내기
```

| 메서드 | 결과 행이 0개 | 1개 | 2개 이상 | 주로 쓰는 곳 |
| --- | --- | --- | --- | --- |
| `fetch()` | 빈 `Result` | 1개짜리 | 전부 | 목록 조회 |
| `fetchOne()` | `null` | 레코드 | `TooManyRowsException` | 레거시 스타일 단건 |
| `fetchOptional()` | `Optional.empty()` | `Optional.of(...)` | `TooManyRowsException` | "없을 수도 있는" 단건 |
| `fetchSingle()` | `NoDataFoundException` | 레코드 | `TooManyRowsException` | "반드시 1건"인 단건 |
| `fetchValue(...)` | `null` | 첫 컬럼 값 | 예외 | 스칼라 값 |
| `fetchExists(...)` / `fetchCount(...)` | — | — | — | 존재 여부·개수 |

각 메서드는 `RecordMapper` 를 인자로 받는 버전이 있어, `fetch(AuthorRepository::toAuthor)` 처럼 행을 바로 DTO로 변환할 수 있습니다.

### 4-2. 공통 예외 클래스

`src/main/java/com/example/bookstore/common/NotFoundException.java`

```java
package com.example.bookstore.common;

/** 요청한 리소스가 없을 때 (HTTP 404) */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
```

`src/main/java/com/example/bookstore/common/ConflictException.java`

```java
package com.example.bookstore.common;

/** 현재 상태와 충돌하는 요청일 때: 재고 부족, 동시 수정, 잘못된 상태 전이 (HTTP 409) */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
```

`src/main/java/com/example/bookstore/common/GlobalExceptionHandler.java`

```java
package com.example.bookstore.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 예외를 RFC 9457 ProblemDetail 형식으로 응답한다.
 * jOOQ가 던진 SQL 예외는 Spring Boot의 jOOQ 자동 설정이 Spring의 DataAccessException으로
 * 변환해 주므로(11장), 여기서는 Spring 예외 타입만 다루면 된다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없음", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException e) {
        return problem(HttpStatus.CONFLICT, "요청이 현재 상태와 충돌함", e.getMessage());
    }

    /** UNIQUE 제약 위반 (예: 중복 ISBN). DataIntegrityViolationException의 하위 타입이라 먼저 매칭된다. */
    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKey(DuplicateKeyException e) {
        return problem(HttpStatus.CONFLICT, "중복된 데이터", "이미 존재하는 값입니다.");
    }

    /** FK·CHECK·NOT NULL 제약 위반 (예: 도서가 있는 저자 삭제) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "데이터 무결성 위반",
                "다른 데이터가 참조하고 있거나 제약 조건을 위반했습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "입력값 검증 실패", "요청 본문에 잘못된 값이 있습니다.");
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        pd.setProperty("errors", errors);
        return pd;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
```

이 핸들러는 최종본입니다. `ConflictException` 은 8장(재고·낙관적 락), `DuplicateKeyException` 은 8·9장(중복 ISBN)에서 본격적으로 쓰입니다.

### 4-3. DTO

`src/main/java/com/example/bookstore/author/Author.java`

```java
package com.example.bookstore.author;

import java.time.OffsetDateTime;

/** API 응답용 저자 DTO. 생성 코드(AuthorRecord)를 API에 직접 노출하지 않는다. */
public record Author(
        Long id,
        String name,
        Integer birthYear,
        OffsetDateTime createdAt
) {
}
```

`src/main/java/com/example/bookstore/author/AuthorRequest.java`

```java
package com.example.bookstore.author;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank @Size(max = 100) String name,
        @Min(1000) @Max(2100) Integer birthYear
) {
}
```

### 4-4. 리포지토리

`src/main/java/com/example/bookstore/author/AuthorRepository.java`

```java
package com.example.bookstore.author;

import static com.example.bookstore.jooq.Tables.AUTHOR;

import com.example.bookstore.jooq.tables.records.AuthorRecord;
import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class AuthorRepository {

    private final DSLContext dsl;

    public AuthorRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** 목록 조회. keyword가 있으면 이름 부분 일치(대소문자 무시)로 거른다. */
    public List<Author> findAll(String keyword) {
        Condition condition = (keyword == null || keyword.isBlank())
                ? DSL.noCondition()
                : AUTHOR.NAME.containsIgnoreCase(keyword);

        return dsl.selectFrom(AUTHOR)
                .where(condition)
                .orderBy(AUTHOR.ID)
                .fetch(AuthorRepository::toAuthor);
    }

    public Optional<Author> findById(long id) {
        return dsl.selectFrom(AUTHOR)
                .where(AUTHOR.ID.eq(id))
                .fetchOptional(AuthorRepository::toAuthor);
    }

    /** INSERT ... RETURNING * : 생성된 id와 DB 기본값(created_at)을 한 번에 돌려받는다. */
    public Author create(AuthorRequest request) {
        AuthorRecord inserted = dsl.insertInto(AUTHOR)
                .set(AUTHOR.NAME, request.name())
                .set(AUTHOR.BIRTH_YEAR, request.birthYear())
                .returning()
                .fetchOne();
        return toAuthor(inserted);
    }

    /** 방법 1: DSL로 UPDATE ... RETURNING. 한 번의 왕복으로 수정과 조회를 끝낸다. */
    public Optional<Author> update(long id, AuthorRequest request) {
        return dsl.update(AUTHOR)
                .set(AUTHOR.NAME, request.name())
                .set(AUTHOR.BIRTH_YEAR, request.birthYear())
                .where(AUTHOR.ID.eq(id))
                .returning()
                .fetchOptional()
                .map(AuthorRepository::toAuthor);
    }

    /**
     * 방법 2: 액티브 레코드. 레코드를 읽고, 값을 바꾸고, store()를 호출한다.
     * jOOQ는 변경된 컬럼(name)만 UPDATE 문에 넣는다. (SELECT 1번 + UPDATE 1번)
     */
    public Optional<Author> rename(long id, String newName) {
        AuthorRecord record = dsl.fetchOne(AUTHOR, AUTHOR.ID.eq(id));
        if (record == null) {
            return Optional.empty();
        }
        record.setName(newName);
        record.store();
        return Optional.of(toAuthor(record));
    }

    public boolean deleteById(long id) {
        return dsl.deleteFrom(AUTHOR)
                .where(AUTHOR.ID.eq(id))
                .execute() > 0;
    }

    public boolean existsById(long id) {
        return dsl.fetchExists(AUTHOR, AUTHOR.ID.eq(id));
    }

    private static Author toAuthor(AuthorRecord r) {
        return new Author(r.getId(), r.getName(), r.getBirthYear(), r.getCreatedAt());
    }
}
```

**코드 해설**

- `DSL.noCondition()` 은 "아무 조건 없음"을 뜻하는 항등원입니다. `where(noCondition())` 은 WHERE 절 자체를 렌더링하지 않습니다. 6장 동적 쿼리의 기초입니다.
- `containsIgnoreCase` 는 `lower(name) like lower('%' || ? || '%')` 로 렌더링되며, 사용자 입력의 `%`, `_` 는 jOOQ가 이스케이프합니다. 모든 값은 바인드 변수(`?`)로 전달되므로 SQL 인젝션에서 안전합니다.
- `returning()` 은 PostgreSQL의 `RETURNING *` 을 씁니다. identity 키와 `DEFAULT now()` 값을 추가 조회 없이 받을 수 있습니다.
- `update()` 과 `rename()` 은 같은 일을 다르게 합니다. DSL 방식은 쿼리 1번으로 끝나고 조건부 수정(`WHERE version = ?` 등)을 자유롭게 표현합니다. 액티브 레코드는 "읽고 고치고 저장"하는 도메인 로직에 자연스럽지만 쿼리가 2번 나갑니다. 이 커리큘럼은 주로 DSL 방식을 쓰고, 8장 주문 취소에서 액티브 레코드를 다시 사용합니다.
- `dsl.fetchOne(AUTHOR, 조건)` 으로 읽은 레코드는 `DSLContext` 에 **attach** 되어 있어 `store()` 를 바로 호출할 수 있습니다. `new AuthorRecord()` 로 만든 레코드는 `dsl.newRecord(AUTHOR)` 로 만들거나 `dsl.attach(record)` 를 해야 저장할 수 있습니다.
- 생성 코드의 레코드를 API로 그대로 내보내지 않고 `Author` record로 변환합니다. 컬럼이 추가됐을 때 API 응답이 의도치 않게 바뀌는 것을 막기 위함입니다.
- 이 리포지토리에는 `@Transactional` 이 없습니다. 단일 SQL은 자동 커밋으로 충분하며, 여러 SQL을 묶어야 하는 경우는 8장에서 서비스 계층에 붙입니다.

### 4-5. 컨트롤러

`src/main/java/com/example/bookstore/author/AuthorController.java`

```java
package com.example.bookstore.author;

import com.example.bookstore.common.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public record RenameRequest(@NotBlank @Size(max = 100) String name) {
    }

    @GetMapping
    public List<Author> list(@RequestParam(required = false) String keyword) {
        return authorRepository.findAll(keyword);
    }

    @GetMapping("/{id}")
    public Author get(@PathVariable long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Author create(@Valid @RequestBody AuthorRequest request) {
        return authorRepository.create(request);
    }

    @PutMapping("/{id}")
    public Author update(@PathVariable long id, @Valid @RequestBody AuthorRequest request) {
        return authorRepository.update(id, request)
                .orElseThrow(() -> notFound(id));
    }

    @PatchMapping("/{id}/name")
    public Author rename(@PathVariable long id, @Valid @RequestBody RenameRequest request) {
        return authorRepository.rename(id, request.name())
                .orElseThrow(() -> notFound(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        if (!authorRepository.deleteById(id)) {
            throw notFound(id);
        }
    }

    private static NotFoundException notFound(long id) {
        return new NotFoundException("저자를 찾을 수 없습니다: id=" + id);
    }
}
```

### 실행 확인

`./gradlew bootRun` 후 http://localhost:8080/ 의 API 콘솔(1-8)을 열고, 왼쪽 **4장 · 저자**에서 차례로 보냅니다. 입력칸의 기본값이 아래 값과 같습니다.

1. `GET /api/authors` → 시드 저자 5명.
2. 같은 API에서 `keyword`에 `emily` → Emily Carter 1명. 대소문자를 무시하고 찾습니다.
3. `POST /api/authors` (본문 `{"name":"정다은","birthYear":1995}`) → `201`. 새 저자가 id `6`으로 응답에 옵니다. (4장 컨트롤러는 `@ResponseStatus(CREATED)`만 쓰므로 `Location` 헤더는 없습니다. 8장의 `ResponseEntity.created(...)`와 비교해 보세요.)
4. `PATCH /api/authors/{id}/name` (id `6`, 본문 `{"name":"정다은(개정)"}`) → 바뀐 이름이 응답에 옵니다.
5. `DELETE /api/authors/{id}` (id `1`) → 도서가 연결된 저자라 FK 위반으로 `409`. 오른쪽에 ProblemDetail의 title과 detail이 강조되어 나옵니다.
6. `POST /api/authors`에서 `name`을 `""`로 바꿔 보내기 → `400`. 검증 오류가 어떤 형식으로 오는지 확인합니다.

요청마다 콘솔의 **미리보기**에 같은 요청의 curl 명령이 나옵니다. 터미널에서도 해 보고 싶으면 복사해 쓰세요.

콘솔에는 다음처럼 jOOQ가 만든 SQL이 찍힙니다. 메서드 체인과 SQL을 나란히 놓고 비교해 보세요.

```text
Executing query          : select "public"."author"."id", ... from "public"."author" where lower("public"."author"."name") like lower(('%' || replace(...) || '%')) escape '!' order by "public"."author"."id"
-> with bind values      : select ... like lower(('%' || replace(..., 'emily', ...) || '%')) ...
Fetched result           : +----+------------+----------+-----------------------------+
```

식별자가 큰따옴표로 감싸이고 스키마가 붙는 것이 기본 동작입니다. 11장에서 `Settings` 로 더 읽기 쉬운 형태로 바꿉니다.

### 연습 과제

1. `fetchSingle()` 로 `findById` 를 바꾸고 없는 id를 조회했을 때 어떤 예외가 어떤 HTTP 상태로 나오는지 확인해 보세요. 왜 `fetchOptional` 이 더 적합한지 설명해 보세요.
2. 출생연도 범위(`fromYear`, `toYear`)로 거르는 조건을 `findAll` 에 추가해 보세요. 힌트: `AUTHOR.BIRTH_YEAR.between(a, b)` 또는 `ge`/`le` 조합.
3. `dsl.newRecord(AUTHOR)` + `store()` 로 INSERT하는 `createWithRecord` 메서드를 만들고, 실행 후 레코드의 `getId()` 가 채워져 있는지 확인해 보세요.

**예상 결과**

1. jOOQ의 `NoDataFoundException`이 나고 응답은 `500`입니다. `GlobalExceptionHandler`에 이 예외의 핸들러가 없고, SQL 오류가 아니라 결과 건수 검사에서 나온 예외라 Spring의 `DataAccessException`으로 변환되지도 않기 때문입니다. 없는 id 조회는 정상적인 요청이므로 404가 맞고, `Optional`이면 컨트롤러가 `orElseThrow`로 404를 고르도록 타입이 강제합니다. 리턴 타입을 안 바꾸려면 `Optional.of(…fetchSingle(AuthorRepository::toAuthor))`로 감싸서 확인합니다.
2. 파라미터는 `Integer`(nullable)입니다. 시드 기준 `fromYear=1980&toYear=1990`이면 김지훈(1982), 최유진(1988), 이서연(1990)이 나옵니다. `between`은 양끝을 포함합니다. 한쪽만 주어져도 동작하게 하려면 `ge`/`le`를 각각 붙이는 쪽이 편합니다.
3. `store()` 후 `getId()`는 채워져 있습니다. 다만 `getCreatedAt()`은 null일 가능성이 큽니다. jOOQ는 기본으로 identity만 돌려받기 때문입니다. `refresh()`를 호출하거나 Settings의 `returnAllOnUpdatableRecord`를 켜면 채워집니다. SQL 로그에서 `create`(RETURNING 전체)와 비교해 보세요.

## 5장. 조인과 DTO 매핑 — 도서 조회 API

도서 조회는 카테고리·저자 테이블과 항상 함께 읽힙니다. 이 장에서는 조인을 쓰는 세 가지 방법(명시적 조인, 암시적 path 조인, 집계 조인)과, 조회 결과를 **컴파일 타임에 검증되는 방식으로** DTO에 담는 `Records.mapping` 을 배웁니다.

### 학습 목표

- `join(...).on(...)`, `leftJoin`, 테이블 별칭을 쓴다.
- `BOOK.category().NAME` 처럼 FK 경로를 따라가는 암시적 조인을 이해한다.
- `Records.mapping(Dto::new)` 로 타입 안전하게 DTO를 만든다.
- `array_agg` 와 `Field.convertFrom()` (애드혹 변환기)을 쓴다.

### 5-1. 개념: 결과를 DTO로 만드는 방법 비교

| 방법 | 예 | 타입 검사 | 특징 |
| --- | --- | --- | --- |
| 수동 매핑 | `r -> new X(r.get(BOOK.ID), ...)` | 부분적 | 자유롭지만 장황함 |
| 리플렉션 매핑 | `fetchInto(X.class)` | 없음(런타임) | 컬럼 이름↔생성자 파라미터 이름으로 매칭. 이름이 어긋나면 조용히 null |
| **`Records.mapping`** | `fetch(mapping(X::new))` | **완전(컴파일 타임)** | SELECT 컬럼의 개수·순서·타입이 생성자와 다르면 컴파일 에러 |

`dsl.select(BOOK.ID, BOOK.TITLE)` 의 결과 타입은 `Record2<Long, String>` 입니다. jOOQ는 최대 22개 컬럼까지 `RecordN<T1..TN>` 타입을 유지하므로, `mapping(BookSummary::new)` 는 "생성자가 `(Long, String, ...)` 를 받는가"를 컴파일러가 검사합니다. 이 커리큘럼은 이후 모든 조회에서 이 방식을 씁니다.

### 5-2. 암시적(path) 조인

jOOQ 3.19부터 FK마다 path 메서드가 생성됩니다. `BOOK.category().NAME` 라고 쓰면 jOOQ가 `book` 과 `category` 의 조인을 자동으로 만듭니다.

- **to-one path**(`BOOK.category()`): N:1 방향. SELECT·WHERE·ORDER BY 어디서나 쓸 수 있고, LEFT JOIN(nullable FK) 또는 INNER JOIN으로 렌더링됩니다.
- **to-many path**(`CATEGORY.book()`): 1:N 방향. 행이 불어나는 것을 막기 위해 기본적으로 FROM 절이나 상관 서브쿼리에서만 허용됩니다.
- 한 테이블에 같은 테이블을 가리키는 FK가 여러 개면 path 메서드 이름이 FK 이름으로 바뀝니다. IDE에서 생성 클래스를 열어 실제 이름을 확인하세요.

### 5-3. DTO

`src/main/java/com/example/bookstore/book/BookSummary.java`

```java
package com.example.bookstore.book;

import java.math.BigDecimal;

/** 목록·검색용 도서 요약. 5장(조회)과 6장(검색)에서 공유한다. */
public record BookSummary(
        Long id,
        String title,
        String isbn,
        BigDecimal price,
        Integer stock,
        String categoryName
) {
}
```

`src/main/java/com/example/bookstore/book/BookWithAuthors.java`

```java
package com.example.bookstore.book;

import java.util.List;

public record BookWithAuthors(
        Long id,
        String title,
        String categoryName,
        List<String> authors
) {
}
```

### 5-4. 리포지토리

`src/main/java/com/example/bookstore/book/BookRepository.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.AUTHOR;
import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.BOOK_AUTHOR;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.arrayAgg;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.val;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

    private final DSLContext dsl;

    public BookRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** 방법 1: 명시적 INNER JOIN. SQL과 가장 비슷하게 읽힌다. */
    public Optional<BookSummary> findSummaryById(long id) {
        return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE, BOOK.STOCK, CATEGORY.NAME)
                .from(BOOK)
                .join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID))
                .where(BOOK.ID.eq(id))
                .fetchOptional(mapping(BookSummary::new));
    }

    /** 방법 2: 암시적 path 조인. FROM 절에 CATEGORY가 없어도 jOOQ가 조인을 만든다. */
    public List<BookSummary> findByCategoryName(String categoryName) {
        return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE, BOOK.STOCK, BOOK.category().NAME)
                .from(BOOK)
                .where(BOOK.category().NAME.eq(categoryName))
                .orderBy(BOOK.TITLE)
                .fetch(mapping(BookSummary::new));
    }

    /**
     * 방법 3: LEFT JOIN + GROUP BY + array_agg.
     * 저자가 없는 도서도 나오도록 LEFT JOIN 하고, FILTER 로 NULL 을 배열에서 뺀다.
     */
    public List<BookWithAuthors> findAllWithAuthorNames() {
        Field<List<String>> authorNames = coalesce(
                arrayAgg(AUTHOR.NAME)
                        .orderBy(AUTHOR.NAME)
                        .filterWhere(AUTHOR.ID.isNotNull()),
                val(new String[0]))
                // 애드혹 변환기: 이 쿼리에서만 String[] 를 List<String> 으로 바꾼다
                .convertFrom(Arrays::asList);

        return dsl.select(BOOK.ID, BOOK.TITLE, CATEGORY.NAME, authorNames)
                .from(BOOK)
                .join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID))
                .leftJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
                .leftJoin(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .groupBy(BOOK.ID, BOOK.TITLE, CATEGORY.NAME)
                .orderBy(BOOK.ID)
                .limit(100)
                .fetch(mapping(BookWithAuthors::new));
    }
}
```

**코드 해설**

- `mapping(BookSummary::new)`: SELECT 절의 컬럼 타입 `Long, String, String, BigDecimal, Integer, String` 이 `BookSummary` 생성자 파라미터와 정확히 일치해야 컴파일됩니다. `BOOK.STOCK` 과 `BOOK.PRICE` 의 순서를 바꿔 보면 즉시 컴파일 에러가 납니다.
- `CATEGORY.NAME` 과 `AUTHOR.NAME` 은 둘 다 컬럼 이름이 `name` 이지만, `mapping` 은 **위치** 기반이라 충돌하지 않습니다. 반면 `fetchInto(X.class)` 는 이름 기반이라 이런 경우 `.as("categoryName")` 처럼 별칭이 필요합니다.
- `arrayAgg(...).orderBy(...).filterWhere(...)` 는 `array_agg(author.name ORDER BY author.name) FILTER (WHERE author.id IS NOT NULL)` 로 렌더링됩니다. 저자가 없으면 결과가 NULL이 되므로 `coalesce` 로 빈 배열을 넣습니다.
- `convertFrom(Arrays::asList)` 는 이 필드에만 적용되는 읽기 전용 변환기입니다. 3장의 `forcedType` 이 "모든 쿼리에 적용되는 변환"이라면, `convertFrom` 은 "이 쿼리에서만 적용되는 변환"입니다.
- 테이블을 두 번 조인해야 할 때는 별칭을 씁니다: `var parent = CATEGORY.as("parent");` 후 `parent.NAME` 처럼 생성 클래스의 타입을 그대로 유지한 채 사용합니다.
- 이 방식(조인 + GROUP BY)은 목록이 하나일 때는 좋지만, 저자 목록·태그 목록처럼 컬렉션이 여러 개면 행이 곱으로 불어납니다. 7장의 MULTISET이 그 해결책입니다.

### 5-5. 컨트롤러

`src/main/java/com/example/bookstore/book/BookController.java`

```java
package com.example.bookstore.book;

import com.example.bookstore.common.NotFoundException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/{id}")
    public BookSummary get(@PathVariable long id) {
        return bookRepository.findSummaryById(id)
                .orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다: id=" + id));
    }

    @GetMapping(params = "category")
    public List<BookSummary> listByCategory(@RequestParam String category) {
        return bookRepository.findByCategoryName(category);
    }

    @GetMapping("/with-authors")
    public List<BookWithAuthors> listWithAuthors() {
        return bookRepository.findAllWithAuthorNames();
    }
}
```

### 실행 확인

API 콘솔의 **5장 · 도서 조회**에서 보냅니다.

1. `GET /api/books/{id}` (id `1`) → 도서 요약 한 건.
2. `GET /api/books` (`category` = `프로그래밍`) → 해당 카테고리 도서 목록.
3. `GET /api/books/with-authors` → 도서마다 저자 이름 배열.

`?category=` 요청의 SQL 로그를 보면 우리가 쓰지 않은 `join "public"."category" as "alias_..."` 가 들어 있습니다. 이것이 path 조인이 만든 조인입니다.

### 연습 과제

1. `findSummaryById` 의 SELECT에서 `BOOK.PRICE` 와 `BOOK.STOCK` 순서를 바꿔 컴파일 에러 메시지를 읽어 보세요.
2. 도서의 상위 카테고리 이름까지 포함하는 쿼리를 만들어 보세요. 힌트: `var parent = CATEGORY.as("parent");` 후 `leftJoin(parent).on(parent.ID.eq(CATEGORY.PARENT_ID))`.
3. `findAllWithAuthorNames` 를 `fetchInto(BookWithAuthors.class)` 로 바꾸면 어떤 필드가 null이 되는지 확인하고, 이유를 설명해 보세요.

**예상 결과**

1. SELECT가 `Record6<Long, String, String, Integer, BigDecimal, String>`이 되는데 `BookSummary` 생성자는 4번째에 `BigDecimal`을 요구합니다. 그래서 `mapping(BookSummary::new)`에서 "invalid constructor reference … Integer cannot be converted to BigDecimal" 비슷한 컴파일 에러가 납니다. 컬럼 순서 실수를 런타임이 아니라 컴파일 때 잡는다는 것이 포인트입니다.
2. 시드 기준 '모던 자바 실전'은 프로그래밍 / IT가 나옵니다. 같은 테이블을 두 번 쓰므로 별칭이 필수이고, SQL에 `category as parent`가 보입니다. 최상위 카테고리에 직접 속한 도서가 있다면 LEFT JOIN이라 상위 이름이 null로 나옵니다.
3. 기본 설정에서는 null이 되는 필드 없이 정상으로 나옵니다. Java record는 Settings의 `mapRecordComponentParameterNames`가 기본 false라 이름이 아니라 순서(인덱스)로 매핑되고, SELECT 4개와 컴포넌트 4개의 순서가 같기 때문입니다. 이 설정을 켜면 이름 기반이 되어 `categoryName`(컬럼 이름 `name`)과 `authors`(별칭 없는 식)가 null이 되고, `.as("categoryName")`, `.as("authors")`로 해결합니다. 기본 설정에서 `BOOK.TITLE`과 `CATEGORY.NAME` 순서를 바꾸면 둘 다 String이라 `mapping()`과 `fetchInto()` 모두 에러 없이 값이 서로 바뀌어 들어갑니다.

## 6장. 동적 쿼리와 페이지네이션 — 도서 검색 API

검색 화면은 "입력된 조건만 거르기"와 "페이지 나눠 보여주기"가 핵심입니다. jOOQ에서 조건은 문자열이 아니라 `Condition` **객체**이므로, 평범한 Java 코드(if문)로 조립합니다. 페이지네이션은 오프셋 방식과 키셋(seek) 방식을 모두 구현합니다.

### 학습 목표

- `noCondition()` 에서 시작해 `and()` 로 조건을 누적하는 동적 쿼리 패턴을 익힌다.
- 정렬 기준을 `SortField` 로 다루고, 사용자 입력을 안전하게 SQL로 바꾼다.
- 오프셋 페이지네이션의 한계를 이해하고 row value 비교로 키셋 페이지네이션을 구현한다.
- jOOQ가 직접 지원하지 않는 문법(JSONB 연산자 등)을 Plain SQL 템플릿으로 안전하게 표현한다.

### 6-1. 개념

**동적 조건.** `Condition where = noCondition();` 으로 시작해 값이 있을 때만 `where = where.and(...)` 를 합니다. `Condition` 은 불변 객체이므로 `and()` 의 반환값을 다시 대입해야 합니다. 조건이 하나도 없으면 WHERE 절이 아예 빠집니다. MyBatis의 `<if>` 태그나 JPA Criteria보다 단순하고, 모든 조건이 컴파일 타임에 검사됩니다.

**오프셋 vs 키셋 페이지네이션.**

| 구분 | 오프셋 (`LIMIT ? OFFSET ?`) | 키셋 (`WHERE (price, id) > (?, ?) LIMIT ?`) |
| --- | --- | --- |
| 임의 페이지 이동 | 가능 (3페이지로 바로) | 불가 (다음/이전만) |
| 뒷 페이지 성능 | OFFSET만큼 읽고 버려 점점 느려짐 | 인덱스로 바로 점프, 항상 일정 |
| 조회 중 데이터 추가/삭제 | 중복·누락 발생 | 안정적 |
| 적합한 화면 | 관리자 테이블(페이지 번호) | 무한 스크롤, API 커서 |

키셋 방식은 정렬 키가 **유일**해야 합니다. 가격은 중복될 수 있으므로 `(price, id)` 두 컬럼을 함께 씁니다. 2장 V3의 `idx_book_price_id` 인덱스가 바로 이 쿼리를 위한 것입니다.

### 6-2. 공통 페이지 응답

`src/main/java/com/example/bookstore/common/PageResponse.java`

```java
package com.example.bookstore.common;

import java.util.List;

/** 오프셋 페이지네이션 응답 (page는 0부터 시작) */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
```

### 6-3. 검색 조건·정렬·스크롤 응답

`src/main/java/com/example/bookstore/book/BookSearchCondition.java`

```java
package com.example.bookstore.book;

import java.math.BigDecimal;

/**
 * 도서 검색 조건. 모든 필드는 선택 사항이며, null이면 해당 조건을 적용하지 않는다.
 * 쿼리 파라미터(?keyword=...&minPrice=...)가 생성자에 바인딩된다.
 */
public record BookSearchCondition(
        String keyword,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String tag,
        String language,
        Boolean inStock
) {
}
```

`src/main/java/com/example/bookstore/book/BookSort.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.BOOK;

import java.util.List;
import org.jooq.SortField;

/**
 * 허용된 정렬 방식만 enum으로 제공한다.
 * 사용자 입력 문자열을 컬럼 이름으로 직접 쓰지 않으므로 ORDER BY 인젝션이 불가능하다.
 * 모든 정렬의 마지막에 id를 두어 순서를 완전히 결정적으로 만든다.
 */
public enum BookSort {

    NEWEST,
    PRICE_ASC,
    PRICE_DESC,
    TITLE;

    public List<SortField<?>> orderFields() {
        return switch (this) {
            case NEWEST -> List.of(BOOK.PUBLISHED_ON.desc().nullsLast(), BOOK.ID.desc());
            case PRICE_ASC -> List.of(BOOK.PRICE.asc(), BOOK.ID.asc());
            case PRICE_DESC -> List.of(BOOK.PRICE.desc(), BOOK.ID.desc());
            case TITLE -> List.of(BOOK.TITLE.asc(), BOOK.ID.asc());
        };
    }
}
```

`src/main/java/com/example/bookstore/book/BookScrollResponse.java`

```java
package com.example.bookstore.book;

import java.math.BigDecimal;
import java.util.List;

/**
 * 키셋(커서) 페이지네이션 응답.
 * 다음 페이지는 ?afterPrice={nextAfterPrice}&afterId={nextAfterId} 로 요청한다.
 */
public record BookScrollResponse(
        List<BookSummary> content,
        boolean hasNext,
        BigDecimal nextAfterPrice,
        Long nextAfterId
) {
}
```

### 6-4. 검색 리포지토리

`src/main/java/com/example/bookstore/book/BookSearchRepository.java`

```javascript
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.noCondition;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.val;

import com.example.bookstore.common.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectOnConditionStep;
import org.springframework.stereotype.Repository;

@Repository
public class BookSearchRepository {

    private final DSLContext dsl;

    public BookSearchRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** 오프셋 페이지네이션: 목록 쿼리 + 전체 건수 쿼리 */
    public PageResponse<BookSummary> search(BookSearchCondition condition, BookSort sort, int page, int size) {
        Condition where = toCondition(condition);

        List<BookSummary> content = baseQuery()
                .where(where)
                .orderBy(sort.orderFields())
                .limit(size)
                .offset((long) page * size)
                .fetch(mapping(BookSummary::new));

        long total = dsl.fetchCount(BOOK, where);
        return PageResponse.of(content, page, size, total);
    }

    /** 키셋 페이지네이션: (price, id) 가 커서보다 큰 행만 읽는다. */
    public BookScrollResponse scrollByPrice(BookSearchCondition condition,
                                            BigDecimal afterPrice, Long afterId, int size) {
        Condition where = toCondition(condition);
        if (afterPrice != null && afterId != null) {
            // WHERE (book.price, book.id) > (?, ?)  — SQL 표준 row value 비교
            where = where.and(row(BOOK.PRICE, BOOK.ID).gt(afterPrice, afterId));
        }

        // 다음 페이지 존재 여부를 알기 위해 한 건 더 읽는다.
        List<BookSummary> rows = baseQuery()
                .where(where)
                .orderBy(BOOK.PRICE.asc(), BOOK.ID.asc())
                .limit(size + 1)
                .fetch(mapping(BookSummary::new));

        boolean hasNext = rows.size() > size;
        List<BookSummary> content = hasNext ? rows.subList(0, size) : rows;
        if (!hasNext || content.isEmpty()) {
            return new BookScrollResponse(content, false, null, null);
        }
        BookSummary last = content.getLast();
        return new BookScrollResponse(content, true, last.price(), last.id());
    }

    /** 목록·스크롤이 공유하는 SELECT ... FROM ... JOIN 부분 */
    private SelectOnConditionStep<Record6<Long, String, String, BigDecimal, Integer, String>> baseQuery() {
        return dsl.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PRICE, BOOK.STOCK, CATEGORY.NAME)
                .from(BOOK)
                .join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID));
    }

    /** 검색 조건 → jOOQ Condition. 값이 있는 조건만 AND 로 쌓는다. */
    private Condition toCondition(BookSearchCondition c) {
        Condition where = noCondition();

        if (hasText(c.keyword())) {
            where = where.and(BOOK.TITLE.containsIgnoreCase(c.keyword()));
        }
        if (c.categoryId() != null) {
            where = where.and(BOOK.CATEGORY_ID.eq(c.categoryId()));
        }
        if (c.minPrice() != null) {
            where = where.and(BOOK.PRICE.ge(c.minPrice()));
        }
        if (c.maxPrice() != null) {
            where = where.and(BOOK.PRICE.le(c.maxPrice()));
        }
        if (hasText(c.tag())) {
            // tags @> ARRAY[?]::text[]  — GIN 인덱스(idx_book_tags)를 탈 수 있는 형태
            where = where.and(condition("{0} @> {1}", BOOK.TAGS, val(new String[]{c.tag()}, BOOK.TAGS)));
        }
        if (hasText(c.language())) {
            // attributes ->> 'language' = ?  — JSONB 연산자는 Plain SQL 템플릿으로 표현
            where = where.and(condition("{0} ->> 'language' = {1}", BOOK.ATTRIBUTES, val(c.language())));
        }
        if (Boolean.TRUE.equals(c.inStock())) {
            where = where.and(BOOK.STOCK.gt(0));
        }
        return where;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
```

**코드 해설**

- **`baseQuery()` 재사용**: 반환 타입 `SelectOnConditionStep<Record6<...>>` 에 컬럼 타입이 모두 들어 있어서, 호출하는 쪽에서 여전히 `mapping(BookSummary::new)` 가 타입 검사됩니다. jOOQ 쿼리 빌더는 내부 상태를 변경하므로, 매번 메서드를 호출해 **새 쿼리**를 만드는 것이 중요합니다(필드에 쿼리 객체를 저장해 공유하지 마세요).
- **`fetchCount(BOOK, where)`**: `select count(*) from book where ...` 를 실행합니다. 조건이 BOOK 컬럼만 참조하므로 조인 없이 셌 수 있습니다. 한 번의 쿼리로 끝내고 싶다면 SELECT에 `count().over().as("total")` 윈도우 함수를 추가하는 방법도 있습니다(10장에서 배움).
- **`row(...).gt(...)`**: `(price, id) > (?, ?)` 는 "price가 크거나, price가 같고 id가 큰" 조건과 같습니다. PostgreSQL은 이 비교에 `(price, id)` 복합 인덱스를 사용합니다. jOOQ에는 같은 일을 하는 전용 문법 `.orderBy(BOOK.PRICE, BOOK.ID).seek(afterPrice, afterId)` 도 있습니다.
- **Plain SQL 템플릿**: `condition("{0} @> {1}", ...)` 의 `{0}`, `{1}` 자리에는 jOOQ 객체가 렌더링됩니다. 값은 반드시 `val(...)` 로 감싸 **바인드 변수**로 넘기고, 문자열 연결(`"... = '" + input + "'"`)은 절대 하지 않습니다. 템플릿은 타입 검사가 약하므로 꼭 필요한 곳에만 씁니다.
- **`val(배열, BOOK.TAGS)`**: 두 번째 인자로 컬럼을 주면 그 컬럼의 데이터 타입(`text[]`)으로 바인딩되어 `text[] @> text[]` 로 타입이 맞습니다. 단순히 "포함 여부"만 본다면 `val(tag).eq(any(BOOK.TAGS))` (→ `? = any(tags)`) 도 가능하지만 이 형태는 GIN 인덱스를 쓰지 못합니다.
- **`BOOK.ATTRIBUTES` 와 변환기**: 이 컬럼은 Java에서 `BookAttributes` 타입이지만, SQL에서는 여전히 `attributes` 컬럼 참조로 렌더링되므로 JSONB 연산자를 그대로 쓸 수 있습니다.

### 6-5. 컨트롤러

`src/main/java/com/example/bookstore/book/BookSearchController.java`

```java
package com.example.bookstore.book;

import com.example.bookstore.common.PageResponse;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookSearchController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookSearchRepository bookSearchRepository;

    public BookSearchController(BookSearchRepository bookSearchRepository) {
        this.bookSearchRepository = bookSearchRepository;
    }

    /** 예: /api/books/search?keyword=자바&minPrice=20000&sort=PRICE_ASC&page=0&size=10 */
    @GetMapping("/search")
    public PageResponse<BookSummary> search(@ModelAttribute BookSearchCondition condition,
                                            @RequestParam(defaultValue = "NEWEST") BookSort sort,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return bookSearchRepository.search(
                condition, sort, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
    }

    /** 예: /api/books/scroll?size=3  →  /api/books/scroll?size=3&afterPrice=22000.00&afterId=4 */
    @GetMapping("/scroll")
    public BookScrollResponse scroll(@ModelAttribute BookSearchCondition condition,
                                     @RequestParam(required = false) BigDecimal afterPrice,
                                     @RequestParam(required = false) Long afterId,
                                     @RequestParam(defaultValue = "20") int size) {
        return bookSearchRepository.scrollByPrice(
                condition, afterPrice, afterId, Math.clamp(size, 1, MAX_PAGE_SIZE));
    }
}
```

`/api/books/search` 와 5장의 `/api/books/{id}` 는 경로가 겹쳐 보이지만, Spring MVC는 리터럴 경로(`/search`)를 패턴 변수(`{id}`)보다 우선해 매칭합니다.

### 실행 확인

API 콘솔의 **6장 · 도서 검색**에서 보냅니다.

1. `GET /api/books/search`를 조건 칸을 모두 비운 채 보내기 → 응답 위에 "전체 N건"이 표시됩니다. SQL 로그에서 WHERE 절이 없는지 확인합니다.
2. 같은 API에서 `tag` = `java`, `inStock` = `true`, `sort` = `PRICE_DESC` → 조건이 조합된 WHERE와 ORDER BY를 로그에서 확인합니다.
3. 조건을 비우고 `language` = `en`만 넣어 보내기 → 영문 도서만 나옵니다.
4. `GET /api/books/scroll` (`size` = `3`) → 응답의 `hasNext`가 true이면 **다음 페이지 이어서 →** 버튼이 나타납니다. 누르면 응답의 `nextAfterPrice`/`nextAfterId`를 `afterPrice`/`afterId`에 채워 바로 다음 요청을 보냅니다.

커서 값은 버튼이 직전 응답에서 그대로 가져오므로, 시드 데이터가 2장과 다르게 들어갔어도 그대로 이어집니다. SQL 로그에서 두 번째 요청부터 `(price, id) > (?, ?)` 조건이 붙는지 확인해 보세요.

### 연습 과제

1. 검색 조건에 `authorName` 을 추가하세요. 힌트: 조인을 늘리지 말고 `exists(selectOne().from(BOOK_AUTHOR).join(AUTHOR).on(...).where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID).and(AUTHOR.NAME.containsIgnoreCase(name))))` 형태의 상관 서브쿼리를 쓰면 행이 중복되지 않습니다.
2. `scrollByPrice` 를 jOOQ의 `.seek(...)` 문법으로 다시 작성하고 두 방식의 SQL을 비교해 보세요.
3. `EXPLAIN` 으로 태그 검색이 GIN 인덱스를 쓰는지 확인해 보세요(데이터가 적으면 순차 스캔이 선택될 수 있으므로 `SET enable_seqscan = off;` 로 강제해 봅니다).

**예상 결과**

1. `authorName=김`이면 김지훈의 도서만 나오고, SQL에 `where exists (select 1 from book_author join author …)`가 붙습니다. JOIN으로 구현하면 조건에 맞는 저자가 둘인 도서가 두 번 나오고 `totalElements`도 부풀려집니다. EXISTS는 도서당 한 행만 남깁니다.
2. `.orderBy(BOOK.PRICE.asc(), BOOK.ID.asc()).seek(afterPrice, afterId)`로 바꾸면 결과는 직접 쓴 `row(...).gt(...)` 방식과 같습니다. PostgreSQL에서는 SQL도 `(price, id) > (?, ?)` 형태로 거의 같게 나옵니다. 차이는 ORDER BY와 커서 값의 짝을 jOOQ가 맞춰 준다는 점이고, 정렬 방향을 바꾸면 비교 방향도 자동으로 따라갑니다.
3. 데이터가 적어 기본으로는 `Seq Scan on book`이 나옵니다. `SET enable_seqscan = off;` 후에는 `Bitmap Index Scan on idx_book_tags`와 `Index Cond: (tags @> '{java}'::text[])`가 보입니다. 검색 조건을 `@>`로 쓴 이유가 이것입니다. `= ANY(tags)`로 쓰면 GIN 인덱스를 타지 못합니다.

## 7장. MULTISET으로 중첩 컬렉션 한 번에 읽기 — 도서 상세 API

도서 상세 화면에는 도서 정보, 카테고리(객체 1개), 저자 목록(컬렉션)이 함께 필요합니다. jOOQ의 `MULTISET` 을 쓰면 이 중첩 구조를 **SQL 한 번**으로, 그리고 **타입 안전하게** Java record 트리로 받을 수 있습니다. JPA의 N+1 문제나 fetch join의 카테시안 곱 문제가 구조적으로 생기지 않습니다.

### 학습 목표

- `multiset(상관 서브쿼리)` 로 1:N 컬렉션을 읽는다.
- `row(...).mapping(...)` 로 중첩 객체(1:1)를 읽는다.
- PostgreSQL에서 MULTISET이 어떤 SQL로 바뀌는지 이해하고 비용을 판단한다.

### 7-1. 개념

세 가지 접근을 비교해 봅시다. 도서 20권을 보여주고 도서마다 저자 2명, 태그 3개가 있다고 가정합니다.

| 접근 | 쿼리 수 | 전송되는 행 | 문제점 |
| --- | --- | --- | --- |
| 도서 조회 후 도서마다 저자 조회 | 1 + 20 = 21회 | 적음 | N+1 네트워크 왕복 |
| 도서·저자·태그를 모두 JOIN | 1회 | 20 × 2 × 3 = 120행 | 카테시안 곱, Java에서 중복 제거 필요 |
| **MULTISET** | **1회** | **20행**(저자 목록은 중첩 값) | DB에서 JSON으로 집계하는 비용 |

PostgreSQL에는 표준 `MULTISET` 이 없으므로 jOOQ가 상관 서브쿼리를 `jsonb_agg(...)` 로 **에뮬레이션**하고, 결과 JSON을 다시 원래 타입(`Long`, `String` …)으로 역직렬화합니다. 개발자는 JSON을 다루지 않고 타입이 있는 `Result<Record2<Long, String>>` 를 받습니다.

- `multiset(select ...)`: 서브쿼리의 결과를 컬렉션 하나의 값으로 만듭니다. 상관 서브쿼리이므로 바깥 쿼리의 `BOOK.ID` 를 참조합니다.
- `multisetAgg(...)`: GROUP BY 안에서 집계 함수로 컬렉션을 만듭니다(5장의 `arrayAgg` 와 비슷한 자리).
- `row(a, b).mapping(X::new)`: 컬럼 여러 개를 중첩 객체 하나로 묶습니다. SQL에서는 그냥 컬럼 두 개입니다.

### 7-2. DTO

`src/main/java/com/example/bookstore/book/AuthorRef.java`

```java
package com.example.bookstore.book;

/** 도서 상세 안에 포함되는 저자 참조 */
public record AuthorRef(
        Long id,
        String name
) {
}
```

`src/main/java/com/example/bookstore/book/CategoryRef.java`

```java
package com.example.bookstore.book;

/** 도서 상세 안에 포함되는 카테고리 참조 */
public record CategoryRef(
        Long id,
        String name
) {
}
```

`src/main/java/com/example/bookstore/book/BookDetail.java`

```java
package com.example.bookstore.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 도서 상세. version 은 8장의 가격 변경(낙관적 락)에서 클라이언트가 돌려보내는 값이다.
 */
public record BookDetail(
        Long id,
        String title,
        String isbn,
        BigDecimal price,
        Integer stock,
        LocalDate publishedOn,
        Integer version,
        List<String> tags,
        BookAttributes attributes,
        CategoryRef category,
        List<AuthorRef> authors
) {
}
```

### 7-3. 리포지토리

`src/main/java/com/example/bookstore/book/BookDetailRepository.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.AUTHOR;
import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.BOOK_AUTHOR;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.select;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

@Repository
public class BookDetailRepository {

    private final DSLContext dsl;

    public BookDetailRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<BookDetail> findById(long id) {
        return dsl.select(
                        BOOK.ID,
                        BOOK.TITLE,
                        BOOK.ISBN,
                        BOOK.PRICE,
                        BOOK.STOCK,
                        BOOK.PUBLISHED_ON,
                        BOOK.VERSION,
                        BOOK.TAGS.convertFrom(Arrays::asList),
                        BOOK.ATTRIBUTES,
                        // 중첩 객체(1:1): 암시적 조인으로 읽은 두 컬럼을 CategoryRef 로 묶는다
                        row(BOOK.category().ID, BOOK.category().NAME).mapping(CategoryRef::new),
                        // 중첩 컬렉션(1:N)
                        authorsOfBook())
                .from(BOOK)
                .where(BOOK.ID.eq(id))
                .fetchOptional(mapping(BookDetail::new));
    }

    /** 도서마다 저자 목록을 상관 서브쿼리로 읽어 List<AuthorRef> 로 만드는 필드 */
    private static Field<List<AuthorRef>> authorsOfBook() {
        return multiset(
                select(AUTHOR.ID, AUTHOR.NAME)
                        .from(BOOK_AUTHOR)
                        .join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                        .where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))   // 바깥 쿼리의 BOOK 과 상관
                        .orderBy(AUTHOR.NAME))
                .as("authors")
                .convertFrom(result -> result.map(mapping(AuthorRef::new)));
    }
}
```

**코드 해설**

- `select(...)` 에 11개의 필드가 있고 `BookDetail` 생성자도 11개 파라미터입니다. 중첩 필드의 타입은 각각 `CategoryRef`, `List<AuthorRef>` 이므로 `mapping(BookDetail::new)` 가 중첩 구조까지 컴파일 타임에 검사합니다.
- `multiset(...)` 의 원래 타입은 `Field<Result<Record2<Long, String>>>` 입니다. `.convertFrom(result -> result.map(mapping(AuthorRef::new)))` 로 `Field<List<AuthorRef>>` 가 됩니다.
- 자주 쓰는 MULTISET 필드는 `authorsOfBook()` 처럼 메서드로 뽑아 두면 목록 쿼리에서도 재사용할 수 있습니다. 바깥 쿼리가 `BOOK` 을 FROM에 두기만 하면 됩니다.
- jOOQ 3.19+에서는 to-many path로 상관 조건을 생략하는 문법도 있습니다. 예를 들어 `multiset(select(BOOK_AUTHOR.AUTHOR_ID).from(BOOK.bookAuthor()))` 처럼 쓰면 상관 조건이 자동으로 붙습니다. 입문 단계에서는 상관 조건을 명시하는 현재 방식이 SQL과 대응시키기 쉽습니다.
- **비용 감각**: MULTISET은 바깥 행마다 서브쿼리를 실행하므로, 목록 화면에서는 반드시 LIMIT과 함께 쓰고 서브쿼리의 조인 컬럼(`book_author.book_id`)에 인덱스가 있는지 확인합니다. 여기서는 PK `(book_id, author_id)` 가 그 역할을 합니다.

### 7-4. 컨트롤러

`src/main/java/com/example/bookstore/book/BookDetailController.java`

```java
package com.example.bookstore.book;

import com.example.bookstore.common.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookDetailController {

    private final BookDetailRepository bookDetailRepository;

    public BookDetailController(BookDetailRepository bookDetailRepository) {
        this.bookDetailRepository = bookDetailRepository;
    }

    @GetMapping("/{id}/detail")
    public BookDetail detail(@PathVariable long id) {
        return bookDetailRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다: id=" + id));
    }
}
```

### 실행 확인

API 콘솔의 **7장 · 도서 상세**에서 `GET /api/books/{id}/detail` (id `2`)을 보내면 다음과 같은 응답이 옵니다.

```json
{
  "id": 2,
  "title": "스프링 부트 4 완벽 가이드",
  "isbn": "9791100000028",
  "price": 38000.00,
  "stock": 30,
  "publishedOn": "2026-01-15",
  "version": 1,
  "tags": ["java", "spring", "backend"],
  "attributes": { "language": "ko", "pages": 640, "format": "PAPERBACK" },
  "category": { "id": 6, "name": "프로그래밍" },
  "authors": [ { "id": 1, "name": "김지훈" }, { "id": 2, "name": "이서연" } ]
}
```

id 값은 환경에 따라 다를 수 있습니다. SQL 로그에서 `jsonb_agg(...)` 또는 `jsonb_build_array(...)` 가 들어간 서브쿼리를 찾아 MULTISET이 어떻게 에뮬레이션되는지 확인해 보세요.

### 연습 과제

1. 저자 상세 API(`GET /api/authors/{id}/books`)를 만들어 저자 정보와 저서 목록(`List<BookRef(id, title, price)>`)을 MULTISET 한 번으로 읽어 보세요.
2. 5장의 `findAllWithAuthorNames` 를 `authorsOfBook()` 을 재사용하도록 바꾸고, GROUP BY가 사라지는 것을 확인해 보세요.
3. `multisetAgg` 를 써서 카테고리별 도서 목록(`List<CategoryWithBooks>`)을 만들어 보세요. 힌트: `select(CATEGORY.NAME, multisetAgg(BOOK.ID, BOOK.TITLE).convertFrom(...)).from(CATEGORY).join(BOOK)...groupBy(CATEGORY.NAME)`.

**예상 결과**

1. `GET /api/authors/1/books` → 김지훈의 정보와 `books: [{id, title, price}, …]`가 한 응답으로 옵니다. SQL 로그에는 저자 SELECT 안에 MULTISET 서브쿼리(`jsonb_agg` 등)가 들어간 쿼리 하나만 찍힙니다. 저서가 없는 저자(예: 4장에서 만든 저자)는 `books: []`입니다.
2. LEFT JOIN 2개, `GROUP BY`, `array_agg`가 사라지고 도서마다 상관 서브쿼리가 붙는 SQL이 됩니다. `authors`가 이름 문자열 목록에서 `List<AuthorRef>`(id, name)로 바뀌므로, 응답 모양을 유지하려면 DTO 타입을 바꾸거나 이름만 꺼내는 변환이 필요합니다.
3. 카테고리마다 `{name, books: [{id, title}, …]}`가 나옵니다. `join(BOOK)`이라 도서가 없는 카테고리(국내도서, IT 처럼 상위만 있는 것)는 빠집니다. `leftJoin`으로 바꾸면 나오지만, 목록에 id·title이 null인 항목 하나가 들어가므로 걸러야 합니다.

## 8장. 트랜잭션과 쓰기 연산 — 도서 등록·주문 API

여러 SQL을 하나의 작업으로 묶는 기능을 만듭니다. 도서 등록(도서 + 저자 연결), 가격 변경(낙관적 락), 주문(재고 차감 + 주문 생성), 주문 취소(비관적 락 + 재고 복구)가 이 장의 결과물입니다. 핵심은 **조건부 UPDATE로 동시성 문제를 DB에서 해결하는 것**입니다.

### 학습 목표

- Spring `@Transactional` 과 jOOQ가 어떻게 연동되는지 이해한다.
- `INSERT ... RETURNING`, 다중 행 INSERT, `UPDATE ... FROM` 을 쓴다.
- "읽고-확인하고-쓰기" 대신 조건부 UPDATE로 재고를 원자적으로 차감한다.
- 낙관적 락(version 컬럼)과 비관적 락(`SELECT ... FOR UPDATE`)을 구분해 쓴다.

### 8-1. 개념

**Spring 트랜잭션과 jOOQ.** Spring Boot의 jOOQ 자동 설정은 `DataSource` 를 `TransactionAwareDataSourceProxy` 로 감싸고 `SpringTransactionProvider` 를 등록합니다. 그래서 `@Transactional` 메서드 안에서 실행되는 모든 jOOQ 쿼리는 같은 커넥션·같은 트랜잭션을 씁니다. 런타임 예외가 나면 모두 롤백됩니다. jOOQ 자체 API(`dsl.transactionResult(cfg -> ...)`)도 있지만 Spring 애플리케이션에서는 `@Transactional` 로 통일하는 편이 일관됩니다.

**재고 차감의 함정.** 다음은 전형적인 경쟁 상태(race condition) 코드입니다.

```java
// 잘못된 예: 두 요청이 동시에 stock=1 을 읽으면 둘 다 통과해 재고가 -1 이 된다
int stock = dsl.select(BOOK.STOCK).from(BOOK).where(BOOK.ID.eq(id)).fetchSingle().value1();
if (stock >= qty) {
    dsl.update(BOOK).set(BOOK.STOCK, stock - qty).where(BOOK.ID.eq(id)).execute();
}
```

올바른 방법은 확인과 변경을 **SQL 한 문장**으로 합치는 것입니다: `UPDATE book SET stock = stock - ? WHERE id = ? AND stock >= ? RETURNING price`. PostgreSQL은 같은 행에 대한 UPDATE를 행 락으로 직렬화하고, 두 번째 UPDATE는 첫 번째가 커밋된 뒤의 값으로 조건을 다시 평가합니다. 영향 행이 0이면 재고 부족입니다.

| 기법 | 이 장의 사용처 | 언제 쓰나 |
| --- | --- | --- |
| 조건부 UPDATE | 재고 차감, 주문 상태 전이 | 변경 규칙을 WHERE 절로 표현할 수 있을 때 (가장 먼저 고려) |
| 낙관적 락 (version) | 가격 변경 | 사용자가 화면에서 본 값을 기준으로 수정할 때 (충돌이 드물고 사용자에게 알려야 함) |
| 비관적 락 (`FOR UPDATE`) | 주문 취소 | 읽은 값으로 여러 단계의 복잡한 판단을 해야 할 때 |

### 8-2. 도서 등록과 가격 변경

`src/main/java/com/example/bookstore/book/BookCreateRequest.java`

```java
package com.example.bookstore.book;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookCreateRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Pattern(regexp = "\\d{13}", message = "ISBN은 숫자 13자리여야 합니다") String isbn,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer stock,
        LocalDate publishedOn,
        List<@NotBlank String> tags,
        BookAttributes attributes,
        @NotEmpty List<@NotNull Long> authorIds
) {

    /** 선택 입력값의 기본값을 채운다. */
    public BookCreateRequest {
        tags = (tags == null) ? List.of() : tags;
        attributes = (attributes == null) ? BookAttributes.empty() : attributes;
    }
}
```

`src/main/java/com/example/bookstore/book/PriceChangeRequest.java`

```java
package com.example.bookstore.book;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** version: 클라이언트가 도서 상세(7장)에서 받은 값을 그대로 돌려보낸다. */
public record PriceChangeRequest(
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull Integer version
) {
}
```

`src/main/java/com/example/bookstore/book/BookCommandService.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.AUTHOR;
import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.BOOK_AUTHOR;
import static com.example.bookstore.jooq.Tables.CATEGORY;

import com.example.bookstore.common.ConflictException;
import com.example.bookstore.common.NotFoundException;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookCommandService {

    private final DSLContext dsl;

    public BookCommandService(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 도서와 도서-저자 연결을 한 트랜잭션으로 저장한다.
     * 중간에 예외가 나면(예: 중복 ISBN) 이미 실행된 INSERT 도 모두 롤백된다.
     */
    @Transactional
    public long create(BookCreateRequest request) {
        if (!dsl.fetchExists(CATEGORY, CATEGORY.ID.eq(request.categoryId()))) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다: id=" + request.categoryId());
        }
        Set<Long> authorIds = new LinkedHashSet<>(request.authorIds());
        int foundAuthors = dsl.fetchCount(AUTHOR, AUTHOR.ID.in(authorIds));
        if (foundAuthors != authorIds.size()) {
            throw new NotFoundException("존재하지 않는 저자가 포함되어 있습니다: " + authorIds);
        }

        // INSERT ... RETURNING id
        Long bookId = dsl.insertInto(BOOK)
                .set(BOOK.CATEGORY_ID, request.categoryId())
                .set(BOOK.TITLE, request.title())
                .set(BOOK.ISBN, request.isbn())
                .set(BOOK.PRICE, request.price())
                .set(BOOK.STOCK, request.stock())
                .set(BOOK.PUBLISHED_ON, request.publishedOn())
                .set(BOOK.TAGS, request.tags().toArray(String[]::new))
                .set(BOOK.ATTRIBUTES, request.attributes())   // Converter가 JSONB로 변환
                .returningResult(BOOK.ID)
                .fetchOne()
                .value1();

        // 다중 행 INSERT: INSERT INTO book_author (book_id, author_id) VALUES (?, ?), (?, ?), ...
        var insert = dsl.insertInto(BOOK_AUTHOR, BOOK_AUTHOR.BOOK_ID, BOOK_AUTHOR.AUTHOR_ID);
        for (Long authorId : authorIds) {
            insert = insert.values(bookId, authorId);
        }
        insert.execute();

        return bookId;
    }

    /**
     * 낙관적 락: 클라이언트가 본 version 과 DB의 version 이 같을 때만 수정하고 version 을 1 올린다.
     * UPDATE book SET price = ?, version = version + 1 WHERE id = ? AND version = ?
     */
    @Transactional
    public void changePrice(long bookId, BigDecimal newPrice, int expectedVersion) {
        int updated = dsl.update(BOOK)
                .set(BOOK.PRICE, newPrice)
                .set(BOOK.VERSION, BOOK.VERSION.plus(1))
                .where(BOOK.ID.eq(bookId))
                .and(BOOK.VERSION.eq(expectedVersion))
                .execute();

        if (updated == 0) {
            if (!dsl.fetchExists(BOOK, BOOK.ID.eq(bookId))) {
                throw new NotFoundException("도서를 찾을 수 없습니다: id=" + bookId);
            }
            throw new ConflictException("다른 사용자가 먼저 수정했습니다. 최신 정보를 다시 조회한 뒤 시도하세요.");
        }
    }
}
```

`src/main/java/com/example/bookstore/book/BookCommandController.java`

```java
package com.example.bookstore.book;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookCommandController {

    private final BookCommandService bookCommandService;
    private final BookDetailRepository bookDetailRepository;

    public BookCommandController(BookCommandService bookCommandService,
                                 BookDetailRepository bookDetailRepository) {
        this.bookCommandService = bookCommandService;
        this.bookDetailRepository = bookDetailRepository;
    }

    @PostMapping
    public ResponseEntity<BookDetail> create(@Valid @RequestBody BookCreateRequest request) {
        long id = bookCommandService.create(request);
        BookDetail detail = bookDetailRepository.findById(id).orElseThrow();
        return ResponseEntity.created(URI.create("/api/books/" + id + "/detail")).body(detail);
    }

    @PatchMapping("/{id}/price")
    public BookDetail changePrice(@PathVariable long id, @Valid @RequestBody PriceChangeRequest request) {
        bookCommandService.changePrice(id, request.price(), request.version());
        return bookDetailRepository.findById(id).orElseThrow();
    }
}
```

- `returningResult(BOOK.ID)` 는 `RETURNING id` 만 받아 `Record1<Long>` 을 돌려줍니다. `returning()` 은 전체 레코드(`BookRecord`)를 돌려줍니다.
- `insert = insert.values(...)` 를 반복하면 하나의 INSERT 문에 VALUES 행이 늘어납니다. 수천 건 이상의 대량 적재는 `dsl.batchInsert(records)` (JDBC 배치)나 `dsl.loadInto(TABLE)` (Loader API)가 더 적합합니다.
- `BOOK.VERSION.plus(1)` 은 Java에서 계산한 값이 아니라 SQL 식 `version + 1` 입니다. `set(필드, 필드식)` 으로 서버측 계산을 표현합니다.
- jOOQ에는 `UpdatableRecord` 기반 낙관적 락 기능도 있습니다(코드 생성 옵션 `recordVersionFields` + `Settings.executeWithOptimisticLocking`). 여기서는 SQL이 눈에 보이도록 직접 작성했습니다.
- `create` 가 커밋된 뒤 컨트롤러에서 상세를 다시 읽습니다. 쓰기(서비스)와 읽기(리포지토리)를 분리하는 간단한 CQRS 스타일입니다.

### 8-3. 주문 DTO

`src/main/java/com/example/bookstore/order/PlaceOrderRequest.java`

```java
package com.example.bookstore.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long memberId,
        @NotEmpty List<@Valid @NotNull Item> items
) {

    public record Item(
            @NotNull Long bookId,
            @NotNull @Min(1) @Max(100) Integer quantity
    ) {
    }
}
```

`src/main/java/com/example/bookstore/order/OrderLine.java`

```java
package com.example.bookstore.order;

import java.math.BigDecimal;

public record OrderLine(
        Long bookId,
        String title,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount
) {
}
```

`src/main/java/com/example/bookstore/order/OrderDetail.java`

```java
package com.example.bookstore.order;

import com.example.bookstore.jooq.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDetail(
        Long id,
        Long memberId,
        String memberName,
        OrderStatus status,
        BigDecimal totalAmount,
        OffsetDateTime orderedAt,
        List<OrderLine> items
) {
}
```

`src/main/java/com/example/bookstore/order/OrderSummary.java`

```java
package com.example.bookstore.order;

import com.example.bookstore.jooq.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderSummary(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        OffsetDateTime orderedAt,
        Integer itemCount
) {
}
```

생성된 enum `OrderStatus` 를 DTO에 그대로 쓰면 간단하지만, DB enum 값이 API 계약이 됩니다. 두 것을 분리하고 싶다면 도메인 enum을 따로 만들고 `EnumConverter` 를 `forcedType` 으로 연결합니다.

### 8-4. 주문 리포지토리

`src/main/java/com/example/bookstore/order/OrderRepository.java`

```java
package com.example.bookstore.order;

import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.MEMBER;
import static com.example.bookstore.jooq.Tables.ORDERS;
import static com.example.bookstore.jooq.Tables.ORDER_ITEM;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectCount;

import com.example.bookstore.jooq.enums.OrderStatus;
import com.example.bookstore.jooq.tables.records.OrdersRecord;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

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
     * UPDATE book SET stock = stock - ? WHERE id = ? AND stock >= ? RETURNING price
     * @return 재고 부족이거나 도서가 없으면 Optional.empty()
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

    /** 취소된 주문의 수량만큼 재고를 되돌린다 (PostgreSQL UPDATE ... FROM) */
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
                .set(ORDERS.TOTAL_AMOUNT, totalAmount)   // status 는 DB 기본값 'PENDING'
                .returningResult(ORDERS.ID)
                .fetchOne()
                .value1();
    }

    public void insertItems(long orderId, List<NewOrderLine> lines) {
        var insert = dsl.insertInto(ORDER_ITEM,
                ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.QUANTITY, ORDER_ITEM.UNIT_PRICE);
        for (NewOrderLine line : lines) {
            insert = insert.values(orderId, line.bookId(), line.quantity(), line.unitPrice());
        }
        insert.execute();
    }

    /** 비관적 락: SELECT ... FROM orders WHERE id = ? FOR UPDATE */
    public Optional<OrdersRecord> lockById(long orderId) {
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(orderId))
                .forUpdate()
                .fetchOptional();
    }

    /** 상태 전이도 조건부 UPDATE: 현재 상태가 from 일 때만 to 로 바꾼다 */
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

    /** 주문 + 회원 이름 + 주문 항목(MULTISET)을 한 번에 읽는다 */
    public Optional<OrderDetail> findDetail(long orderId) {
        Field<List<OrderLine>> items = multiset(
                select(ORDER_ITEM.BOOK_ID,
                        BOOK.TITLE,
                        ORDER_ITEM.QUANTITY,
                        ORDER_ITEM.UNIT_PRICE,
                        ORDER_ITEM.UNIT_PRICE.mul(ORDER_ITEM.QUANTITY))
                        .from(ORDER_ITEM)
                        .join(BOOK).on(BOOK.ID.eq(ORDER_ITEM.BOOK_ID))
                        .where(ORDER_ITEM.ORDER_ID.eq(ORDERS.ID))
                        .orderBy(ORDER_ITEM.ID))
                .as("items")
                .convertFrom(result -> result.map(mapping(OrderLine::new)));

        return dsl.select(ORDERS.ID, ORDERS.MEMBER_ID, MEMBER.NAME, ORDERS.STATUS,
                        ORDERS.TOTAL_AMOUNT, ORDERS.ORDERED_AT, items)
                .from(ORDERS)
                .join(MEMBER).on(MEMBER.ID.eq(ORDERS.MEMBER_ID))
                .where(ORDERS.ID.eq(orderId))
                .fetchOptional(mapping(OrderDetail::new));
    }

    /** 회원의 주문 목록. 항목 수는 스칼라 서브쿼리로 센다. */
    public List<OrderSummary> findByMember(long memberId) {
        Field<Integer> itemCount = field(
                selectCount().from(ORDER_ITEM).where(ORDER_ITEM.ORDER_ID.eq(ORDERS.ID)));

        return dsl.select(ORDERS.ID, ORDERS.STATUS, ORDERS.TOTAL_AMOUNT, ORDERS.ORDERED_AT, itemCount)
                .from(ORDERS)
                .where(ORDERS.MEMBER_ID.eq(memberId))
                .orderBy(ORDERS.ORDERED_AT.desc())
                .fetch(mapping(OrderSummary::new));
    }
}
```

### 8-5. 주문 서비스

`src/main/java/com/example/bookstore/order/OrderService.java`

```java
package com.example.bookstore.order;

import static com.example.bookstore.jooq.Tables.MEMBER;

import com.example.bookstore.common.ConflictException;
import com.example.bookstore.common.NotFoundException;
import com.example.bookstore.jooq.enums.OrderStatus;
import com.example.bookstore.jooq.tables.records.OrdersRecord;
import com.example.bookstore.order.OrderRepository.NewOrderLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final DSLContext dsl;
    private final OrderRepository orderRepository;

    public OrderService(DSLContext dsl, OrderRepository orderRepository) {
        this.dsl = dsl;
        this.orderRepository = orderRepository;
    }

    /**
     * 주문 생성: 재고 차감 → 주문 INSERT → 항목 INSERT 를 하나의 트랜잭션으로 처리한다.
     * 어느 한 도서라도 재고가 부족하면 ConflictException 으로 전체 롤백된다.
     */
    @Transactional
    public OrderDetail placeOrder(PlaceOrderRequest request) {
        if (!dsl.fetchExists(MEMBER, MEMBER.ID.eq(request.memberId()))) {
            throw new NotFoundException("회원을 찾을 수 없습니다: id=" + request.memberId());
        }

        // 같은 도서가 여러 번 들어오면 합치고, bookId 순으로 정렬한다.
        // 모든 트랜잭션이 같은 순서로 행 락을 잡으므로 교차 락(데드락)을 예방한다.
        Map<Long, Integer> quantities = new TreeMap<>();
        for (PlaceOrderRequest.Item item : request.items()) {
            quantities.merge(item.bookId(), item.quantity(), Integer::sum);
        }

        List<NewOrderLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            long bookId = entry.getKey();
            int quantity = entry.getValue();

            BigDecimal unitPrice = orderRepository.decreaseStock(bookId, quantity)
                    .orElseThrow(() -> new ConflictException(
                            "재고가 부족하거나 존재하지 않는 도서입니다: bookId=" + bookId));

            lines.add(new NewOrderLine(bookId, quantity, unitPrice));
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        long orderId = orderRepository.insertOrder(request.memberId(), total);
        orderRepository.insertItems(orderId, lines);
        return orderRepository.findDetail(orderId).orElseThrow();
    }

    /** 결제: PENDING → PAID (조건부 UPDATE) */
    @Transactional
    public OrderDetail pay(long orderId) {
        if (!orderRepository.changeStatus(orderId, OrderStatus.PENDING, OrderStatus.PAID)) {
            throw orderRepository.existsById(orderId)
                    ? new ConflictException("결제 대기(PENDING) 상태의 주문만 결제할 수 있습니다.")
                    : new NotFoundException("주문을 찾을 수 없습니다: id=" + orderId);
        }
        return orderRepository.findDetail(orderId).orElseThrow();
    }

    /**
     * 취소: 주문 행을 FOR UPDATE 로 잠근 뒤 상태를 확인하고, 재고를 복구한 다음 상태를 바꾼다.
     * 동시에 두 번 취소해도 두 번째 요청은 락을 기다렸다가 CANCELLED 를 보고 거부된다.
     */
    @Transactional
    public OrderDetail cancel(long orderId) {
        OrdersRecord order = orderRepository.lockById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: id=" + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("취소할 수 없는 상태입니다: " + order.getStatus());
        }

        orderRepository.restoreStock(orderId);

        // 액티브 레코드: 변경된 status 컬럼만 UPDATE 한다
        order.setStatus(OrderStatus.CANCELLED);
        order.store();

        return orderRepository.findDetail(orderId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public OrderDetail get(long orderId) {
        return orderRepository.findDetail(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: id=" + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> listByMember(long memberId) {
        if (!dsl.fetchExists(MEMBER, MEMBER.ID.eq(memberId))) {
            throw new NotFoundException("회원을 찾을 수 없습니다: id=" + memberId);
        }
        return orderRepository.findByMember(memberId);
    }
}
```

### 8-6. 주문 컨트롤러

`src/main/java/com/example/bookstore/order/OrderController.java`

```java
package com.example.bookstore.order;

import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDetail> place(@Valid @RequestBody PlaceOrderRequest request) {
        OrderDetail order = orderService.placeOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
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
```

**코드 해설**

- **롤백 범위**: `placeOrder` 에서 두 번째 도서의 재고가 부족하면, 이미 차감된 첫 번째 도서의 재고도 함께 롤백됩니다. 12장에서 테스트로 증명합니다.
- **락 순서**: `TreeMap` 으로 bookId를 정렬하는 것은 데드락 예방입니다. A는 \[1, 2\], B는 \[2, 1\] 순서로 잠그면 서로를 기다리며 멈출 수 있습니다.
- **`UPDATE ... FROM`**: PostgreSQL 전용 문법으로, 다른 테이블과 조인한 결과로 여러 행을 한 번에 갱신합니다. jOOQ는 이를 지원하지 않는 DB에서는 가능한 경우 다른 문법으로 에뮬레이션합니다.
- **`forUpdate()`**: `SELECT ... FOR UPDATE` 로 행을 잠근 채 트랜잭션이 끝날 때까지 다른 트랜잭션의 변경을 막습니다. 작업 큐처럼 "잠긴 행은 건너뛰기"가 필요하면 `.forUpdate().skipLocked()` 를 씁니다. 락은 반드시 `@Transactional` 안에서만 의미가 있습니다.
- **`order.store()`**: `lockById` 로 읽은 레코드는 attach 상태이고, `setStatus` 로 바뀐 컬럼만 `UPDATE orders SET status = ? WHERE id = ?` 로 저장됩니다. 같은 트랜잭션이므로 재고 복구와 함께 커밋됩니다.
- **`readOnly = true`**: 읽기 전용 트랜잭션은 PostgreSQL 드라이버에서 커넥션을 read-only로 표시해, 실수로 쓰기 쿼리가 나가면 오류를 냅니다.

### 실행 확인

API 콘솔의 **8장 · 쓰기·주문**에서 순서대로 보냅니다. `categoryId`·`authorIds`는 환경에 맞게 바꿉니다.

1. `POST /api/books` (기본 본문: "테스트 주도 개발 입문", ISBN `9791100000080`) → `201`. `Location`이 `/api/books/{id}/detail`이고 본문은 도서 상세입니다.
2. 같은 본문을 그대로 한 번 더 보내기 → 같은 ISBN이라 `409` (DuplicateKeyException).
3. `PATCH /api/books/{id}/price` (id `1`, `{"price": 31000, "version": 1}`) → `price` 31000, `version` 2.
4. 본문을 `{"price": 30000, "version": 1}`로 바꿔 다시 보내기 → version이 낡아서 `409` (낙관적 락).
5. `POST /api/orders` (기본 본문: 회원 1, 도서 3×2권 + 도서 1×1권) → `201`. 응답의 `id`를 확인합니다(시드 그대로라면 8).
6. `POST /api/orders/{id}/pay` (id = 5의 id) → `status`가 `PAID`.
7. `POST /api/orders/{id}/cancel` (같은 id) → PAID 주문은 취소됩니다. SHIPPED 주문 id로 보내면 `409`입니다.
8. `POST /api/orders` 본문을 `{"memberId": 1, "items": [{"bookId": 6, "quantity": 1}]}`로 바꿔 보내기 → 재고 0인 도서(바다로 가는 기차)라 `409`.

### 연습 과제

1. "배송 처리" API(`POST /api/orders/{id}/ship`, PAID → SHIPPED)를 `changeStatus` 로 추가하세요.
2. `decreaseStock` 을 일부러 "읽고-확인하고-쓰기" 방식으로 바꾼 뒤, 12장의 동시성 테스트가 실패하는 것을 확인해 보세요.
3. 주문 생성 시 재고 차감 여러 번을 하나의 SQL로 합쳐 보세요. 힌트: `UPDATE book SET stock = stock - v.qty FROM (VALUES ...) AS v(id, qty) WHERE book.id = v.id AND book.stock >= v.qty RETURNING ...` 를 jOOQ의 `values(row(...), ...)` 파생 테이블로 표현하고, 갱신된 행 수가 항목 수와 다르면 예외를 던집니다.

**예상 결과**

1. PAID 주문에 `POST /api/orders/{id}/ship` → `status`가 `SHIPPED`. 같은 요청을 한 번 더 보내거나 PENDING 주문에 보내면 `changeStatus`가 false(영향 행 0)를 돌려주므로 `409`입니다. 배송된 주문을 취소하면 `409`가 나는 것도 확인해 보세요.
2. 12장 `concurrentOrdersNeverOversell`이 실패합니다. 여러 스레드가 같은 재고 값을 읽고 동시에 통과해 성공 수가 5보다 커지고(매번 다름), 재고는 덜 줄어듭니다(lost update). "expected 5 but was 12" 같은 AssertionError가 나옵니다.
3. 로그에 주문 항목 수만큼의 UPDATE 대신 `update book set stock = stock - v.qty from (values (?, ?), (?, ?)) as v(id, qty) … returning …` 한 건이 찍힙니다. 항목 중 하나라도 재고가 모자라면 갱신 행 수가 항목 수보다 적어 예외가 나고, 트랜잭션이 롤백되어 다른 항목의 재고도 그대로입니다.

## 9장. PostgreSQL 특화 기능 — enum, 배열, JSONB, 업서트

jOOQ는 "표준 SQL만" 다루는 도구가 아닙니다. 사용 중인 DB의 고유 기능을 타입 안전하게 쓰는 것이 jOOQ의 강점입니다. 이 장에서는 회원 업서트(`ON CONFLICT`), 도서 태그 관리(배열 함수·`unnest`), 도서 속성 부분 수정(JSONB `||` 병합)을 만듭니다.

### 학습 목표

- `onConflict(...).doUpdate()` 와 `excluded(...)` 로 업서트를 구현한다.
- `arrayAppend`, `arrayRemove`, `any`, `unnest` 로 배열 컬럼을 다룬다.
- `CASE WHEN` 을 `when(...).otherwise(...)` 로 쓴다.
- Converter가 적용된 JSONB 컬럼에 Plain SQL 연산자를 안전하게 조합한다.

### 9-1. 개념 정리

| PostgreSQL 기능 | SQL | jOOQ |
| --- | --- | --- |
| enum | `status = 'PAID'::order_status` | `ORDERS.STATUS.eq(OrderStatus.PAID)` (캐스팅 자동) |
| 업서트 | `INSERT ... ON CONFLICT (email) DO UPDATE SET name = EXCLUDED.name` | `.onConflict(MEMBER.EMAIL).doUpdate().set(MEMBER.NAME, excluded(MEMBER.NAME))` |
| 무시하는 INSERT | `ON CONFLICT DO NOTHING` | `.onConflictDoNothing()` |
| 배열 포함 | `? = ANY(tags)` | `val(tag).eq(any(BOOK.TAGS))` |
| 배열 추가/제거 | `array_append(tags, ?)`, `array_remove(tags, ?)` | `arrayAppend(BOOK.TAGS, tag)`, `arrayRemove(BOOK.TAGS, tag)` |
| 배열 펼치기 | `FROM book, unnest(tags) AS t(tag)` | `from(BOOK, unnest(BOOK.TAGS).as("t", "tag"))` |
| JSONB 병합 | `attributes \|\| ?::jsonb` | `field("{0} \|\| {1}", ...)` (Plain SQL 템플릿) |

jOOQ는 표준 SQL/JSON 함수(`jsonObject`, `jsonbArrayAgg`, `jsonbValue` 등)도 제공합니다. 이 함수들로 JSON을 만들거나 읽을 수 있고, 병합처럼 전용 함수가 없는 연산만 템플릿으로 보완합니다.

### 9-2. 회원 업서트

`src/main/java/com/example/bookstore/member/Member.java`

```java
package com.example.bookstore.member;

import java.time.OffsetDateTime;

public record Member(
        Long id,
        String email,
        String name,
        OffsetDateTime createdAt
) {
}
```

`src/main/java/com/example/bookstore/member/MemberUpsertRequest.java`

```java
package com.example.bookstore.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpsertRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String name
) {
}
```

`src/main/java/com/example/bookstore/member/MemberRepository.java`

```java
package com.example.bookstore.member;

import static com.example.bookstore.jooq.Tables.MEMBER;
import static org.jooq.impl.DSL.excluded;

import com.example.bookstore.jooq.tables.records.MemberRecord;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    private final DSLContext dsl;

    public MemberRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 이메일 기준 업서트. 없으면 INSERT, 있으면 이름만 UPDATE 한다.
     * INSERT INTO member (email, name) VALUES (?, ?)
     * ON CONFLICT (email) DO UPDATE SET name = EXCLUDED.name
     * RETURNING *
     * "조회 후 없으면 INSERT" 와 달리 동시 요청에서도 UNIQUE 위반이 나지 않는다.
     */
    public Member upsert(String email, String name) {
        MemberRecord record = dsl.insertInto(MEMBER)
                .set(MEMBER.EMAIL, email)
                .set(MEMBER.NAME, name)
                .onConflict(MEMBER.EMAIL)
                .doUpdate()
                .set(MEMBER.NAME, excluded(MEMBER.NAME))
                .returning()
                .fetchOne();
        return toMember(record);
    }

    public Optional<Member> findById(long id) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.ID.eq(id))
                .fetchOptional(MemberRepository::toMember);
    }

    public List<Member> findAll() {
        return dsl.selectFrom(MEMBER)
                .orderBy(MEMBER.ID)
                .fetch(MemberRepository::toMember);
    }

    private static Member toMember(MemberRecord r) {
        return new Member(r.getId(), r.getEmail(), r.getName(), r.getCreatedAt());
    }
}
```

`src/main/java/com/example/bookstore/member/MemberController.java`

```java
package com.example.bookstore.member;

import com.example.bookstore.common.NotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** PUT 은 멱등(같은 요청을 여러 번 보내도 결과가 같음)하므로 업서트에 잘 맞는다. */
    @PutMapping
    public Member upsert(@Valid @RequestBody MemberUpsertRequest request) {
        return memberRepository.upsert(request.email(), request.name());
    }

    @GetMapping
    public List<Member> list() {
        return memberRepository.findAll();
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: id=" + id));
    }
}
```

### 9-3. 태그(배열)와 속성(JSONB) 관리

`src/main/java/com/example/bookstore/book/TagCount.java`

```java
package com.example.bookstore.book;

public record TagCount(
        String tag,
        Integer bookCount
) {
}
```

`src/main/java/com/example/bookstore/book/BookAdminRepository.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.BOOK;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.any;
import static org.jooq.impl.DSL.arrayAppend;
import static org.jooq.impl.DSL.arrayRemove;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.unnest;
import static org.jooq.impl.DSL.val;
import static org.jooq.impl.DSL.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Table;
import org.springframework.stereotype.Repository;

@Repository
public class BookAdminRepository {

    private final DSLContext dsl;

    public BookAdminRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 태그 추가. 이미 있으면 그대로 두고, 없으면 배열 끝에 붙인다.
     * UPDATE book SET tags = CASE WHEN ? = ANY(tags) THEN tags ELSE array_append(tags, ?) END
     * WHERE id = ? RETURNING tags
     * @return 갱신된 태그 목록, 도서가 없으면 empty
     */
    public Optional<List<String>> addTag(long bookId, String tag) {
        Field<String[]> newTags = when(val(tag).eq(any(BOOK.TAGS)), BOOK.TAGS)
                .otherwise(arrayAppend(BOOK.TAGS, tag));

        return dsl.update(BOOK)
                .set(BOOK.TAGS, newTags)
                .where(BOOK.ID.eq(bookId))
                .returningResult(BOOK.TAGS)
                .fetchOptional()
                .map(r -> Arrays.asList(r.value1()));
    }

    /** 태그 제거: array_remove 는 일치하는 모든 원소를 지운다 (없으면 변화 없음) */
    public Optional<List<String>> removeTag(long bookId, String tag) {
        return dsl.update(BOOK)
                .set(BOOK.TAGS, arrayRemove(BOOK.TAGS, tag))
                .where(BOOK.ID.eq(bookId))
                .returningResult(BOOK.TAGS)
                .fetchOptional()
                .map(r -> Arrays.asList(r.value1()));
    }

    /**
     * 태그별 도서 수. unnest 로 배열을 행으로 펼친 뒤 GROUP BY 한다.
     * SELECT t.tag, count(*) FROM book, unnest(book.tags) AS t(tag) GROUP BY t.tag
     */
    public List<TagCount> tagStats() {
        Table<?> tags = unnest(BOOK.TAGS).as("t", "tag");
        Field<String> tag = field(name("t", "tag"), String.class);

        return dsl.select(tag, count())
                .from(BOOK, tags)
                .groupBy(tag)
                .orderBy(count().desc(), tag.asc())
                .fetch(mapping(TagCount::new));
    }

    /**
     * JSONB 부분 병합: 전달된 속성만 덮어쓰고 나머지는 유지한다.
     * UPDATE book SET attributes = attributes || ?::jsonb WHERE id = ? RETURNING attributes
     * BookAttributes 는 @JsonInclude(NON_NULL) 이므로 null 인 속성은 JSON에서 빠져 덮어쓰지 않는다.
     */
    public Optional<BookAttributes> mergeAttributes(long bookId, BookAttributes patch) {
        Field<BookAttributes> merged = field(
                "{0} || {1}",
                BOOK.ATTRIBUTES.getDataType(),          // 결과 타입: Converter 포함 JSONB
                BOOK.ATTRIBUTES,
                val(patch, BOOK.ATTRIBUTES));           // 바인드 값도 Converter 로 JSONB 변환

        return dsl.update(BOOK)
                .set(BOOK.ATTRIBUTES, merged)
                .where(BOOK.ID.eq(bookId))
                .returningResult(BOOK.ATTRIBUTES)
                .fetchOptional()
                .map(Record1::value1);
    }
}
```

`src/main/java/com/example/bookstore/book/BookAdminController.java`

```java
package com.example.bookstore.book;

import com.example.bookstore.common.NotFoundException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookAdminController {

    private final BookAdminRepository bookAdminRepository;

    public BookAdminController(BookAdminRepository bookAdminRepository) {
        this.bookAdminRepository = bookAdminRepository;
    }

    @PostMapping("/api/books/{id}/tags/{tag}")
    public List<String> addTag(@PathVariable long id, @PathVariable String tag) {
        return bookAdminRepository.addTag(id, tag.trim().toLowerCase())
                .orElseThrow(() -> notFound(id));
    }

    @DeleteMapping("/api/books/{id}/tags/{tag}")
    public List<String> removeTag(@PathVariable long id, @PathVariable String tag) {
        return bookAdminRepository.removeTag(id, tag.trim().toLowerCase())
                .orElseThrow(() -> notFound(id));
    }

    @PatchMapping("/api/books/{id}/attributes")
    public BookAttributes mergeAttributes(@PathVariable long id, @RequestBody BookAttributes patch) {
        return bookAdminRepository.mergeAttributes(id, patch)
                .orElseThrow(() -> notFound(id));
    }

    @GetMapping("/api/tags/stats")
    public List<TagCount> tagStats() {
        return bookAdminRepository.tagStats();
    }

    private static NotFoundException notFound(long id) {
        return new NotFoundException("도서를 찾을 수 없습니다: id=" + id);
    }
}
```

**코드 해설**

- **`excluded(MEMBER.NAME)`**: 충돌로 인해 "삽입되지 못한 새 값"을 가리킵니다. `set(MEMBER.NAME, name)` 처럼 값을 다시 넣어도 되지만, 다중 행 업서트에서는 `excluded` 가 유일한 방법입니다. 조건부 업데이트가 필요하면 `.doUpdate().set(...).where(MEMBER.NAME.ne(excluded(MEMBER.NAME)))` 처럼 WHERE를 붙일 수 있습니다.
- **`when(...).otherwise(...)`**: SQL `CASE WHEN ... THEN ... ELSE ... END` 입니다. 두 가지 값의 타입이 `Field<String[]>` 로 같아야 컴파일됩니다. "없을 때만 추가"를 SQL 한 문장으로 처리하므로 8장처럼 동시 요청에도 안전합니다.
- **`unnest(...).as("t", "tag")`**: 테이블 별칭과 컬럼 별칭을 함께 주는 형태입니다. 생성 코드에 없는 컬럼이므로 `field(name("t", "tag"), String.class)` 로 타입을 직접 지정해 참조합니다. PostgreSQL은 FROM 절의 함수가 앞 테이블을 참조하는 것을 허용합니다(암시적 LATERAL).
- **템플릿과 Converter**: `field("{0} || {1}", BOOK.ATTRIBUTES.getDataType(), ...)` 의 두 번째 인자가 결과 타입을 정합니다. 컨버터가 포함된 데이터 타입을 넘겼으므로 결과도 `BookAttributes` 로 읽힙니다. `val(patch, BOOK.ATTRIBUTES)` 는 컬럼의 타입(컨버터 포함)으로 바인딩하므로 `BookAttributes` 가 JSONB 문자열로 바뀝니다.
- **enum**: `ORDERS.STATUS.eq(OrderStatus.PAID)` 는 jOOQ가 `cast(? as order_status)` 를 붙여 렌더링하므로 개발자가 캐스팅을 신경 쓸 필요가 없습니다. DB에 enum 값을 추가하면(`ALTER TYPE order_status ADD VALUE 'REFUNDED'`) 코드 재생성만으로 Java enum에도 추가됩니다.

### 실행 확인

API 콘솔의 **9장 · PostgreSQL 특화**에서 보냅니다.

1. `PUT /api/members` (`{"email":"new@example.com","name":"신규회원"}`) → 새 회원의 `id` 확인.
2. `name`만 `이름변경`으로 바꿔 다시 보내기 → `id`는 그대로, 이름만 바뀝니다(업서트).
3. `POST /api/books/{id}/tags/{tag}` (id `1`, tag `Bestseller`) → 소문자 `bestseller`가 배열에 들어갑니다.
4. tag를 `bestseller`로 바꿔 한 번 더 → 배열에는 여전히 한 번만 있습니다.
5. `DELETE /api/books/{id}/tags/{tag}` (id `1`, tag `bestseller`) → 배열에서 빠집니다.
6. `GET /api/tags/stats` → 태그별 도서 수.
7. `PATCH /api/books/{id}/attributes` (id `1`, `{"pages": 540}`) → `pages`만 바뀌고 `language`, `format`은 유지됩니다(JSONB 병합).

### 연습 과제

1. 여러 회원을 한 번에 업서트하는 `upsertAll(List<MemberUpsertRequest>)` 을 만들어 보세요. `insertInto(MEMBER, MEMBER.EMAIL, MEMBER.NAME)` 에 `values` 를 여러 번 붙이고 `excluded` 를 쓰면 됩니다.
2. "태그가 하나도 없는 도서" 목록을 조회해 보세요. 힌트: `cardinality(tags) = 0` 을 `condition("cardinality({0}) = 0", BOOK.TAGS)` 또는 jOOQ의 `cardinality(...)` 함수로 표현합니다.
3. `jsonbGetAttributeAsText` 등 jOOQ의 JSON 함수를 찾아 6장의 `language` 검색 조건을 Plain SQL 없이 다시 작성해 보세요. (컨버터가 있는 컬럼이라면 `field(BOOK.ATTRIBUTES.getQualifiedName(), SQLDataType.JSONB)` 처럼 원래 타입의 필드를 만들어야 합니다.)

**예상 결과**

1. 기존 이메일과 새 이메일을 섞어 보내면 기존 회원은 이름만 바뀌고(id 유지) 새 회원은 추가됩니다. SQL은 `insert … values (?, ?), (?, ?) on conflict (email) do update set name = excluded.name` 한 건입니다. 한 번의 요청 안에 같은 이메일이 두 번 있으면 PostgreSQL이 "ON CONFLICT DO UPDATE command cannot affect row a second time" 오류를 내므로, 미리 중복을 제거해야 합니다.
2. 시드 도서는 모두 태그가 있어 처음에는 빈 목록이 맞습니다. 태그 제거 API로 '바다로 가는 기차'의 `novel`을 지우면 그 도서가 나옵니다. 빈 배열에서 `array_length(tags, 1)`은 0이 아니라 NULL을 돌려주므로 `cardinality`를 쓰는 것이 포인트입니다.
3. SQL이 `attributes ->> 'language' = ?`로 렌더링되어 Plain SQL 버전과 같습니다. `language=en`이면 'The Quiet Algorithm' 한 권이 나옵니다. 컨버터 때문에 `BOOK.ATTRIBUTES`를 그대로 넣으면 타입이 `BookAttributes`라서 컴파일이 안 되는 것도 확인해 보세요.

## 10장. 고급 SQL — 집계, 윈도우 함수, CTE, 재귀 CTE

통계 화면은 jOOQ가 가장 빛나는 영역입니다. ORM에서는 네이티브 쿼리 문자열로 도망쳐야 하는 윈도우 함수와 CTE를 타입 안전한 DSL로 작성합니다. 이 장에서는 월별 매출(누적·전월 비교), 카테고리별 베스트셀러 순위, 주문 상태 분포, 카테고리 트리를 만듭니다.

### 학습 목표

- `sum`, `count`, `groupBy` 와 윈도우 함수(`over`, `partitionBy`, `rank`, `lag`)를 쓴다.
- `name(...).fields(...).as(select)` 로 CTE를 만들고 `with(...)` 로 사용한다.
- `withRecursive` 로 계층 데이터를 펼친다.

### 10-1. 개념

- **집계 함수 vs 윈도우 함수**: 집계 함수는 여러 행을 한 행으로 접고, 윈도우 함수는 행을 유지한 채 "관련된 행들"에 대한 값을 각 행에 붙입니다. jOOQ에서는 집계 함수 뒤에 `.over(...)` 를 붙이면 윈도우 함수가 됩니다.
- **집계 결과에 대한 윈도우**: `sum(sum(total)) over (order by month)` 처럼 GROUP BY 결과 위에서 누적 합계를 구할 수 있습니다. 윈도우 함수는 GROUP BY 다음에 계산되기 때문입니다.
- **CTE(WITH 절)**: 복잡한 쿼리를 이름 붙은 단계로 나눕니다. jOOQ에서 CTE는 일반 Java 변수이므로, 단계별로 변수에 담고 조합합니다.
- **재귀 CTE**: 시작 행(루트)과 "이전 결과에서 다음 행을 찾는" 쿼리를 `UNION ALL` 로 잇습니다. 자기 자신을 참조해야 하므로 Java 변수 대신 `table(name("tree"))` 처럼 **이름**으로 참조합니다.

### 10-2. DTO

`src/main/java/com/example/bookstore/stats/MonthlySales.java`

```java
package com.example.bookstore.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

/** month 는 해당 월의 1일 (예: 2026-07-01) */
public record MonthlySales(
        LocalDate month,
        Integer orderCount,
        BigDecimal revenue,
        BigDecimal cumulativeRevenue,
        BigDecimal previousMonthRevenue
) {
}
```

`src/main/java/com/example/bookstore/stats/BestSeller.java`

```java
package com.example.bookstore.stats;

import java.math.BigDecimal;

public record BestSeller(
        String categoryName,
        Long bookId,
        String title,
        BigDecimal soldQuantity,
        Integer rank
) {
}
```

`src/main/java/com/example/bookstore/category/CategoryNode.java`

```java
package com.example.bookstore.category;

/** 카테고리 트리의 한 노드. depth 는 루트가 0, path 는 "국내도서 > IT > 프로그래밍" 형태 */
public record CategoryNode(
        Long id,
        Long parentId,
        String name,
        Integer depth,
        String path
) {
}
```

### 10-3. 통계 리포지토리

`src/main/java/com/example/bookstore/stats/StatsRepository.java`

```java
package com.example.bookstore.stats;

import static com.example.bookstore.jooq.Tables.BOOK;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static com.example.bookstore.jooq.Tables.ORDERS;
import static com.example.bookstore.jooq.Tables.ORDER_ITEM;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lag;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.orderBy;
import static org.jooq.impl.DSL.partitionBy;
import static org.jooq.impl.DSL.rank;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.sum;

import com.example.bookstore.jooq.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

@Repository
public class StatsRepository {

    /** 매출로 인정하는 주문 상태 */
    private static final Condition REVENUE_ORDER = ORDERS.STATUS.in(OrderStatus.PAID, OrderStatus.SHIPPED);

    private final DSLContext dsl;

    public StatsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 월별 매출 + 누적 매출 + 전월 매출.
     *
     * SELECT month, count(*), sum(total_amount),
     *        sum(sum(total_amount)) OVER (ORDER BY month),
     *        lag(sum(total_amount)) OVER (ORDER BY month)
     * FROM orders WHERE status IN ('PAID', 'SHIPPED')
     * GROUP BY month ORDER BY month
     */
    public List<MonthlySales> monthlySales() {
        // 한국 시간 기준으로 월을 자른다. 별칭 없이 식 자체를 GROUP BY 와 OVER 에 재사용한다.
        Field<LocalDate> month = field(
                "cast(date_trunc('month', {0} at time zone 'Asia/Seoul') as date)",
                LocalDate.class, ORDERS.ORDERED_AT);
        Field<BigDecimal> revenue = sum(ORDERS.TOTAL_AMOUNT);

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
     * 마지막에 rank <= N 으로 거른다. (윈도우 함수 결과는 WHERE 에서 바로 쓸 수 없어서 한 단계 감싼다)
     */
    public List<BestSeller> bestSellersByCategory(int topN) {
        var sales = name("book_sales").fields("book_id", "sold_qty").as(
                select(ORDER_ITEM.BOOK_ID, sum(ORDER_ITEM.QUANTITY))
                        .from(ORDER_ITEM)
                        .join(ORDERS).on(ORDERS.ID.eq(ORDER_ITEM.ORDER_ID))
                        .where(REVENUE_ORDER)
                        .groupBy(ORDER_ITEM.BOOK_ID));
        Field<Long> salesBookId = sales.field("book_id", Long.class);
        Field<BigDecimal> soldQty = sales.field("sold_qty", BigDecimal.class);

        var ranked = name("ranked").fields("category_name", "book_id", "title", "sold_qty", "rnk").as(
                select(CATEGORY.NAME,
                        BOOK.ID,
                        BOOK.TITLE,
                        soldQty,
                        rank().over(partitionBy(BOOK.CATEGORY_ID).orderBy(soldQty.desc())))
                        .from(sales)
                        .join(BOOK).on(BOOK.ID.eq(salesBookId))
                        .join(CATEGORY).on(CATEGORY.ID.eq(BOOK.CATEGORY_ID)));
        Field<String> categoryName = ranked.field("category_name", String.class);
        Field<Long> bookId = ranked.field("book_id", Long.class);
        Field<String> title = ranked.field("title", String.class);
        Field<BigDecimal> rankedQty = ranked.field("sold_qty", BigDecimal.class);
        Field<Integer> rnk = ranked.field("rnk", Integer.class);

        return dsl.with(sales, ranked)
                .select(categoryName, bookId, title, rankedQty, rnk)
                .from(ranked)
                .where(rnk.le(topN))
                .orderBy(categoryName, rnk, bookId)
                .fetch(mapping(BestSeller::new));
    }

    /** 주문 상태별 건수. enum 타입 그대로 Map 의 키가 된다. */
    public Map<OrderStatus, Integer> orderStatusCounts() {
        return dsl.select(ORDERS.STATUS, count())
                .from(ORDERS)
                .groupBy(ORDERS.STATUS)
                .orderBy(ORDERS.STATUS)
                .fetchMap(Record2::value1, Record2::value2);
    }
}
```

**코드 해설**

- **`month` 필드 재사용**: 같은 `Field` 객체를 SELECT, GROUP BY, OVER, ORDER BY에 모두 넣으면 같은 식으로 렌더링됩니다. SELECT에서만 `.as("month")` 로 별칭을 붙이고 나머지는 별칭 없는 원본을 쓰는 것이 요령입니다. PostgreSQL은 OVER 절 안에서 SELECT 별칭을 참조할 수 없기 때문입니다.
- **타임존**: `timestamptz` 를 월 단위로 자를 때는 어느 시간대 기준인지가 중요합니다. 7월 31일 23시(KST) 주문은 UTC로는 7월 31일 14시이지만, 8월 1일 01시(KST) 주문은 UTC로 7월에 속합니다. `at time zone 'Asia/Seoul'` 로 기준을 명시했습니다.
- **`sum(revenue).over(orderBy(month))`**: 집계 결과에 대한 누적 합계입니다. `lag(revenue)` 는 바로 앞 행(전월)의 값이며 첫 행은 null입니다.
- **CTE 필드 참조**: `sales.field("sold_qty", BigDecimal.class)` 처럼 CTE의 컬럼을 타입과 함께 꺼냅니다. PostgreSQL의 `sum(integer)` 은 `bigint` 를 돌려주고 jOOQ의 `sum()` 은 `BigDecimal` 타입을 쓰므로 `BigDecimal` 로 받습니다.
- **`rank()` vs `row_number()` vs `dense_rank()`**: 동점자가 있을 때 `rank` 는 1, 1, 3, `dense_rank` 는 1, 1, 2, `row_number` 는 1, 2, 3입니다. "상위 N개를 정확히 N개만"이면 `rowNumber()` 를 씁니다.
- **QUALIFY**: jOOQ는 `.qualify(rank().over(...).le(n))` 문법도 제공하며, PostgreSQL처럼 QUALIFY가 없는 DB에서는 서브쿼리로 에뮬레이션합니다. 여기서는 CTE를 연습하려고 명시적으로 작성했습니다.
- **`fetchMap(Record2::value1, Record2::value2)`**: 결과를 `Map` 으로 바로 만듭니다. 키가 중복되면 예외가 나므로 GROUP BY 키와 맵 키가 같아야 합니다. 피벗 형태가 필요하면 `count().filterWhere(ORDERS.STATUS.eq(OrderStatus.PAID))` 처럼 FILTER 절을 여러 개 씁니다.

### 10-4. 카테고리 트리(재귀 CTE)

`src/main/java/com/example/bookstore/category/CategoryRepository.java`

```java
package com.example.bookstore.category;

import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

    private final DSLContext dsl;

    public CategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * WITH RECURSIVE tree(id, parent_id, name, depth, path) AS (
     *     SELECT id, parent_id, name, 0, cast(name AS text) FROM category WHERE parent_id IS NULL
     *     UNION ALL
     *     SELECT c.id, c.parent_id, c.name, tree.depth + 1, tree.path || ' > ' || c.name
     *     FROM category c JOIN tree ON c.parent_id = tree.id
     * )
     * SELECT * FROM tree ORDER BY path
     */
    public List<CategoryNode> findTree() {
        // 재귀 부분에서 자기 자신을 참조하기 위한 "이름 기반" 참조
        Table<?> treeRef = table(name("tree"));
        Field<Long> treeId = field(name("tree", "id"), Long.class);
        Field<Integer> treeDepth = field(name("tree", "depth"), Integer.class);
        Field<String> treePath = field(name("tree", "path"), String.class);

        var tree = name("tree").fields("id", "parent_id", "name", "depth", "path").as(
                // 시작점: 루트 카테고리
                select(CATEGORY.ID,
                        CATEGORY.PARENT_ID,
                        CATEGORY.NAME,
                        inline(0),
                        CATEGORY.NAME.cast(SQLDataType.CLOB))   // varchar → text (재귀 항과 타입을 맞춤)
                        .from(CATEGORY)
                        .where(CATEGORY.PARENT_ID.isNull())
                        .unionAll(
                                // 반복: 현재까지 찾은 노드의 자식
                                select(CATEGORY.ID,
                                        CATEGORY.PARENT_ID,
                                        CATEGORY.NAME,
                                        treeDepth.plus(1),
                                        treePath.concat(inline(" > "), CATEGORY.NAME))
                                        .from(CATEGORY)
                                        .join(treeRef).on(CATEGORY.PARENT_ID.eq(treeId))));

        return dsl.withRecursive(tree)
                .select(tree.field("id", Long.class),
                        tree.field("parent_id", Long.class),
                        tree.field("name", String.class),
                        tree.field("depth", Integer.class),
                        tree.field("path", String.class))
                .from(tree)
                .orderBy(tree.field("path", String.class))
                .fetch(mapping(CategoryNode::new));
    }
}
```

`src/main/java/com/example/bookstore/category/CategoryController.java`

```java
package com.example.bookstore.category;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/tree")
    public List<CategoryNode> tree() {
        return categoryRepository.findTree();
    }
}
```

- **타입 맞추기**: 재귀 CTE는 시작 항과 반복 항의 컬럼 타입이 정확히 같아야 합니다. `name` 은 `varchar(100)` 이고 문자열 연결 결과는 `text` 이므로, 시작 항을 `cast(SQLDataType.CLOB)`(PostgreSQL에서 `text`)로 맞추지 않으면 "recursive query column has type ..." 오류가 납니다.
- **`inline(0)`**: 바인드 변수(`?`) 대신 SQL에 리터럴 `0` 을 직접 씁니다. 바인드 변수는 타입이 불분명해 재귀 CTE의 타입 추론을 방해할 수 있습니다.
- **순환 방지**: 데이터에 순환(A→B→A)이 있으면 재귀가 끝나지 않습니다. 실무에서는 `where(treeDepth.lt(10))` 같은 깊이 제한을 두거나 PostgreSQL 14+의 `CYCLE` 절을 씁니다.

### 10-5. 통계 컨트롤러

`src/main/java/com/example/bookstore/stats/StatsController.java`

```java
package com.example.bookstore.stats;

import com.example.bookstore.jooq.enums.OrderStatus;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return statsRepository.bestSellersByCategory(Math.clamp(top, 1, 50));
    }

    @GetMapping("/order-status")
    public Map<OrderStatus, Integer> orderStatus() {
        return statsRepository.orderStatusCounts();
    }
}
```

### 실행 확인

API 콘솔의 **10장 · 통계**에서 보냅니다.

1. `GET /api/stats/monthly-sales` → 아래 표와 같은 월별 매출.
2. `GET /api/stats/best-sellers` (`top` = `1`) → 카테고리별 1위 도서.
3. `GET /api/stats/order-status` → 상태별 주문 수.
4. `GET /api/categories/tree` → 각 항목의 `depth`와 `path`로 3단 계층이 펼쳐지는지 확인합니다.

시드 데이터만 있는 상태라면 월별 매출은 다음과 같습니다. 8장에서 주문을 더 넣고 결제했다면 11월 행이 추가됩니다.

| month | orderCount | revenue | cumulativeRevenue | previousMonthRevenue |
| --- | --- | --- | --- | --- |
| 2026-07-01 | 2 | 100000.00 | 100000.00 | null |
| 2026-08-01 | 2 | 117000.00 | 217000.00 | 100000.00 |
| 2026-10-01 | 1 | 62000.00 | 279000.00 | 117000.00 |

9월은 매출 인정 주문이 없어 행 자체가 빠집니다. 따라서 10월의 `previousMonthRevenue` 는 "직전 행"인 8월 값입니다(연습 과제 1 참고).

### 연습 과제

1. 매출이 없는 달도 0으로 나오게 해 보세요. 힌트: `generate_series('2026-07-01'::date, '2026-10-01', interval '1 month')` 를 `table("...")` 또는 `generateSeries(...)` 로 만들고 LEFT JOIN 합니다.
2. 6장 검색에 "하위 카테고리 포함" 옵션을 추가하세요. 재귀 CTE로 하위 카테고리 id를 모은 뒤 `BOOK.CATEGORY_ID.in(select(...).from(tree))` 로 거릅니다.
3. 회원별 누적 구매액과 등급(상위 10%는 VIP)을 `sum(...).over(partitionBy(...))` 와 `percentRank()` 로 구해 보세요.

**예상 결과**

1. `2026-09-01` 행이 생기고 `orderCount` 0, `revenue` 0, `cumulativeRevenue` 217000.00(8월과 같음)입니다. 10월의 `previousMonthRevenue`는 117000.00(8월)에서 0(9월)으로 바뀌어, `lag()`가 "직전 행"을 본다는 점이 드러납니다. 매출은 `coalesce(sum(...), 0)`으로 감싸야 null이 아닌 0이 됩니다.
2. IT(시드 순서대로라면 id 4)에는 직접 속한 도서가 없어 옵션 없이는 0건입니다. 하위 포함을 켜면 프로그래밍 3권과 데이터베이스 2권, 총 5권이 나옵니다. SQL에는 `with recursive … category_id in (select id from tree)`가 보입니다.
3. 회원마다 누적 구매액과 0\~1 사이의 `percent_rank`가 나옵니다. 내림차순 기준 1등은 0이고, `percent_rank`는 (순위-1)/(전체-1)이라 회원이 3명뿐인 시드에서는 상위 10% 기준에 1등만 VIP가 됩니다.

## 11장. 설정과 관측 — Settings, ExecuteListener, 예외 변환

기능이 다 만들어졌으니 운영을 위한 설정을 더합니다. SQL 렌더링 방식과 쿼리 타임아웃을 `Settings` 로 조정하고, 느린 쿼리를 경고로 남기는 `ExecuteListener` 를 만듭니다. 또 Spring Boot가 jOOQ 예외를 어떻게 Spring 예외로 바꾸는지 이해합니다.

### 학습 목표

- Spring Boot가 jOOQ 설정에 반영하는 확장 포인트(빈)를 안다.
- `DefaultConfigurationCustomizer` 로 `Settings` 를 바꾼다.
- `ExecuteListener` 의 생명주기 콜백으로 쿼리 실행을 관찰한다.
- 실행 없이 SQL을 렌더링·확인하는 방법을 익힌다.

### 11-1. 개념: Spring Boot jOOQ 자동 설정의 확장 포인트

| 등록하는 빈 | 역할 |
| --- | --- |
| `DefaultConfigurationCustomizer` | jOOQ `DefaultConfiguration` 을 마지막에 수정(Settings 등) |
| `ExecuteListenerProvider` | 쿼리 실행 전후 콜백 (로깅, 메트릭, 타이밍) |
| `RecordListenerProvider` | `store()`, `insert()` 등 레코드 연산 전후 콜백 (감사 컬럼 자동 설정 등) |
| `VisitListenerProvider` | SQL 렌더링 과정에 개입 (멀티테넌트 조건 자동 추가 등 고급) |
| `ExceptionTranslatorExecuteListener` | `SQLException` → Spring `DataAccessException` 변환 방식 교체 |

주의할 점: 커스터마이저 안에서 `configuration.set(new DefaultExecuteListenerProvider(...))` 처럼 리스너를 직접 **교체**하면 Boot가 등록한 예외 변환 리스너까지 사라져 4장의 `DuplicateKeyException` 처리가 동작하지 않습니다. 리스너는 반드시 `ExecuteListenerProvider` **빈**으로 추가하세요.

**예외 변환의 흐름.** jOOQ는 JDBC의 `SQLException` 을 `org.jooq.exception.DataAccessException` 으로 감싸 던집니다. Spring Boot는 여기에 리스너를 끼워 SQLState(예: `23505` = unique\_violation)를 보고 Spring의 `DuplicateKeyException`, `DataIntegrityViolationException` 등으로 바꿉니다. 그래서 컨트롤러 어드바이스는 JPA를 쓸 때와 같은 Spring 예외 타입만 알면 됩니다.

### 11-2. 느린 쿼리 리스너

`src/main/java/com/example/bookstore/config/SlowQueryListener.java`

```java
package com.example.bookstore.config;

import java.time.Duration;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 임계값보다 오래 걸린 쿼리를 WARN 으로 남긴다.
 * ExecuteListener 는 실행마다 같은 인스턴스가 호출되므로 상태를 필드에 두면 안 되고,
 * 실행별 데이터는 ExecuteContext#data 에 담는다.
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
            log.warn("느린 쿼리 {} ms: {}", elapsed / 1_000_000, ctx.sql());
        }
    }
}
```

### 11-3. jOOQ 설정 클래스

`src/main/java/com/example/bookstore/config/JooqConfig.java`

```java
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
            @Value("${bookstore.jooq.slow-query-threshold:200ms}") Duration threshold) {
        return new DefaultExecuteListenerProvider(new SlowQueryListener(threshold));
    }

    /** jOOQ Settings: SQL 렌더링과 실행 방식 조정 */
    @Bean
    DefaultConfigurationCustomizer jooqSettingsCustomizer() {
        return configuration -> configuration.settings()
                // "public"."book"."title" → book.title : 로그가 읽기 쉬워진다
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                .withRenderSchema(false)
                // IN (?, ?, ?) 의 바인드 개수를 2의 거듭제곱으로 맞춰 DB 실행 계획 캐시 재사용률을 높인다
                .withInListPadding(true)
                // 모든 쿼리의 기본 타임아웃(초). 폭주하는 통계 쿼리가 커넥션을 무한정 잡지 않게 한다
                .withQueryTimeout(10);
    }
}
```

**코드 해설**

- **패키지 위치**: Spring Boot 4의 모듈화로 `DefaultConfigurationCustomizer` 는 `org.springframework.boot.jooq.autoconfigure` 패키지에 있습니다(Boot 3에서는 `org.springframework.boot.autoconfigure.jooq`). Boot 3 자료를 참고할 때 가장 자주 막히는 부분입니다.
- **`RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED`**: 생성 코드의 식별자는 따옴표 없이, `DSL.name("...")` 처럼 직접 만든 이름은 따옴표를 붙여 렌더링합니다. 이 프로젝트는 모든 식별자가 소문자라 안전합니다. 대소문자가 섞인 테이블 이름을 쓴다면 기본값(항상 따옴표)을 유지하세요.
- **`withRenderSchema(false)`**: `public.` 접두어를 생략합니다. 스키마를 여러 개 쓰거나 런타임에 스키마를 바꾸는 멀티테넌트 구조라면 대신 `withRenderMapping(...)` 을 씁니다.
- **`withQueryTimeout(10)`**: JDBC `Statement.setQueryTimeout(10)` 을 모든 쿼리에 적용합니다. 개별 쿼리는 `.queryTimeout(30)` 으로 덮어씁니다.
- **`ctx.sql()`**: 바인드 변수가 `?` 로 남아 있는 SQL입니다. 로그에 개인정보가 남지 않아 운영 로깅에 적합합니다. 값까지 보려면 `DSL.using(ctx.dialect()).renderInlined(ctx.query())` 를 쓰되, `ctx.query()` 는 배치·Plain JDBC 실행에서 null일 수 있습니다.
- **메트릭**: 실무에서는 같은 자리에서 Micrometer `Timer` 를 기록해 쿼리 지연을 대시보드로 봅니다.

### 11-4. 실행 없이 SQL 확인하기

디버깅과 테스트에서 자주 쓰는 도구입니다(리포지토리 안에서 임시로 실험하는 조각 코드).

```java
var query = dsl.select(BOOK.TITLE).from(BOOK).where(BOOK.PRICE.ge(new BigDecimal("30000")));

query.getSQL();              // select book.title from book where book.price >= ?
query.getBindValues();       // [30000]
dsl.renderInlined(query);    // select book.title from book where book.price >= 30000
dsl.explain(query);          // EXPLAIN 결과(실행 계획)를 객체로 반환

// DB 연결 없이 렌더링만: 단위 테스트(12장)에서 유용
DSL.using(SQLDialect.POSTGRES).renderInlined(query);
```

### 실행 확인

```bash
# 임계값을 0ms 로 낮춰 모든 쿼리가 찍히는지 확인
./gradlew bootRun --args='--bookstore.jooq.slow-query-threshold=0ms'
# API 콘솔에서 GET /api/stats/monthly-sales 를 보낸 뒤 로그 확인
# WARN ... SlowQueryListener : 느린 쿼리 3 ms: select cast(date_trunc('month', ...

# 중복 이메일을 업서트가 아닌 INSERT 로 넣으면 DuplicateKeyException → 409 가 되는지도
# 예외 변환 확인용으로 연습 과제 2에서 직접 만들어 봅니다.
```

이제 SQL 로그의 식별자에서 따옴표와 `public.` 이 사라져 4장보다 훨씬 읽기 쉬워집니다.

### 연습 과제

1. `RecordListener` 를 구현해 `UpdatableRecord.store()` 로 저장되는 레코드에 `version` 필드가 있으면 로그를 남겨 보세요. `RecordListenerProvider` 빈으로 등록합니다.
2. 임시 엔드포인트에서 `dsl.insertInto(MEMBER).set(MEMBER.EMAIL, "minsu@example.com").set(MEMBER.NAME, "x").execute()` 를 실행해 어떤 Spring 예외가 올라오는지, `getCause()` 체인을 따라 PostgreSQL SQLState가 무엇인지 확인해 보세요.
3. `ExecuteListener` 에 Micrometer `MeterRegistry` 를 주입해 `jooq.query` 타이머를 기록해 보세요(Actuator 의존성 `spring-boot-starter-actuator` 추가 필요).

**예상 결과**

1. `version` 컬럼은 `book`에만 있습니다. 4장 저자 이름 변경(author)이나 8장 주문 취소(orders)처럼 `store()`를 쓰는 곳에서는 로그가 남지 않는 것이 정상입니다. `BookRecord`를 읽어 `store()`하는 코드를 잠깐 만들어야 로그가 찍힙니다. `record.field("version") != null`로 판별합니다.
2. Spring의 `DuplicateKeyException`이 올라오고 응답은 `409`("중복된 데이터")입니다. `getCause()` 체인을 따라가면 jOOQ `DataAccessException` → `PSQLException`이 나오고, `getSQLState()`는 `23505`(unique\_violation)입니다. Spring이 이 SQLState를 보고 예외 타입을 고릅니다.
3. 몇 번 요청을 보낸 뒤 `/actuator/metrics/jooq.query`를 열면 `COUNT`, `TOTAL_TIME`, `MAX`가 보입니다. 기본 노출은 `health`뿐이므로 `management.endpoints.web.exposure.include: metrics`를 추가해야 열립니다. 태그(예: 쿼리 종류)를 붙였다면 `?tag=type:SELECT`처럼 걸러 볼 수 있습니다.

## 12장. Testcontainers 통합 테스트

jOOQ 코드는 "실제 PostgreSQL에서 실제 SQL이 어떻게 동작하느냐"가 전부이므로, H2 같은 인메모리 DB가 아닌 **진짜 PostgreSQL 18**로 테스트합니다. JSONB, 배열, enum, `ON CONFLICT` 는 H2에서 동작하지 않거나 다르게 동작합니다. Testcontainers가 테스트마다 도커 컨테이너를 띄우고, Spring Boot의 `@ServiceConnection` 이 접속 정보를 자동으로 연결합니다.

### 학습 목표

- Testcontainers 2.0 + `@ServiceConnection` 으로 테스트용 DB를 구성한다.
- 리포지토리·서비스·API 계층별 테스트를 작성한다.
- 트랜잭션 롤백과 동시성(재고 초과 판매 방지)을 테스트로 증명한다.
- DB 없이 SQL 렌더링만 검사하는 단위 테스트를 작성한다.

### 12-1. 개념

- **컨테이너 재사용**: Spring은 설정이 같은 테스트 클래스들의 `ApplicationContext` 를 캐시합니다. 컨테이너를 빈으로 등록하면 컨텍스트와 함께 캐시되므로, 모든 테스트 클래스가 컨테이너 하나를 공유합니다(컨테이너 기동은 한 번).
- **스키마와 시드**: 컨텍스트가 뜨면서 Flyway가 V1\~V3를 적용하므로 테스트는 2장의 시드 데이터를 전제로 합니다.
- **데이터 격리**: `@Transactional` 을 붙인 테스트는 끝날 때 롤백됩니다. 단, 커밋이 필요한 테스트(동시성, 서비스의 롤백 검증)는 트랜잭션을 붙이지 않고 매번 새 데이터(새 도서)를 만들어 서로 간섭하지 않게 합니다.
- **필요한 것**: 테스트를 실행하는 장비에 Docker가 동작 중이어야 합니다. 로컬 `compose.yaml` 의 DB는 필요 없습니다.

### 12-2. 테스트 인프라

`src/test/java/com/example/bookstore/TestcontainersConfiguration.java`

```java
package com.example.bookstore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 18 컨테이너.
 * @ServiceConnection 이 컨테이너의 JDBC URL/계정을 spring.datasource.* 대신 자동으로 연결한다.
 * (Testcontainers 2.0 부터 PostgreSQLContainer 는 org.testcontainers.postgresql 패키지이며 제네릭이 없다)
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));
    }
}
```

`src/test/java/com/example/bookstore/TestBookstoreApplication.java`

```java
package com.example.bookstore;

import org.springframework.boot.SpringApplication;

/**
 * 로컬에 PostgreSQL 을 띄우지 않고도 Testcontainers 로 애플리케이션을 실행한다.
 * ./gradlew bootTestRun  또는 IDE 에서 이 main 을 실행
 */
public class TestBookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.from(BookstoreApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
```

`src/test/java/com/example/bookstore/BookstoreApplicationTests.java`

```java
package com.example.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookstoreApplicationTests {

    @Test
    void contextLoads() {
        // Flyway 마이그레이션 + jOOQ DSLContext + 모든 빈 생성이 성공하면 통과
    }
}
```

### 12-3. 리포지토리 테스트 (롤백 격리)

`src/test/java/com/example/bookstore/author/AuthorRepositoryTest.java`

```java
package com.example.bookstore.author;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookstore.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** 각 테스트는 트랜잭션 안에서 실행되고 끝나면 롤백된다. (jOOQ 도 Spring 트랜잭션을 따른다) */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthorRepositoryTest {

    @Autowired
    AuthorRepository authorRepository;

    @Test
    @DisplayName("저자를 만들면 id와 created_at 이 채워져 돌아온다")
    void create() {
        Author created = authorRepository.create(new AuthorRequest("테스트 저자", 1990));

        assertThat(created.id()).isNotNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(authorRepository.findById(created.id()))
                .get()
                .extracting(Author::name)
                .isEqualTo("테스트 저자");
    }

    @Test
    @DisplayName("키워드는 대소문자를 무시하고 부분 일치로 검색한다")
    void findByKeyword() {
        assertThat(authorRepository.findAll("EMILY"))
                .extracting(Author::name)
                .containsExactly("Emily Carter");
    }

    @Test
    @DisplayName("DSL UPDATE 와 액티브 레코드 store() 모두 값을 바꾼다")
    void updateAndRename() {
        Author created = authorRepository.create(new AuthorRequest("원래 이름", 1980));

        assertThat(authorRepository.update(created.id(), new AuthorRequest("수정 이름", 1981)))
                .get()
                .satisfies(a -> {
                    assertThat(a.name()).isEqualTo("수정 이름");
                    assertThat(a.birthYear()).isEqualTo(1981);
                });

        assertThat(authorRepository.rename(created.id(), "다시 수정"))
                .get()
                .extracting(Author::name)
                .isEqualTo("다시 수정");
    }

    @Test
    @DisplayName("없는 id 는 빈 Optional / false 를 돌려준다")
    void notFound() {
        assertThat(authorRepository.findById(-1L)).isEmpty();
        assertThat(authorRepository.update(-1L, new AuthorRequest("x", null))).isEmpty();
        assertThat(authorRepository.deleteById(-1L)).isFalse();
    }
}
```

### 12-4. 서비스 테스트 (롤백·동시성)

`src/test/java/com/example/bookstore/order/OrderServiceTest.java`

```java
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
```

### 12-5. 통계·재귀 CTE 테스트

`src/test/java/com/example/bookstore/stats/StatsRepositoryTest.java`

```java
package com.example.bookstore.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookstore.TestcontainersConfiguration;
import com.example.bookstore.category.CategoryNode;
import com.example.bookstore.category.CategoryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StatsRepositoryTest {

    @Autowired
    StatsRepository statsRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    @DisplayName("월별 매출은 PAID/SHIPPED 만 집계하고 누적·전월 값을 함께 계산한다")
    void monthlySales() {
        // 다른 테스트가 만든 최근 주문의 영향을 피하려고 시드 기간(2026-07 ~ 2026-10)만 본다
        List<MonthlySales> sales = statsRepository.monthlySales().stream()
                .filter(s -> !s.month().isAfter(LocalDate.of(2026, 10, 1)))
                .toList();

        assertThat(sales).extracting(MonthlySales::month).containsExactly(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 1));

        MonthlySales august = sales.get(1);
        assertThat(august.orderCount()).isEqualTo(2);
        assertThat(august.revenue()).isEqualByComparingTo("117000");
        assertThat(august.cumulativeRevenue()).isEqualByComparingTo("217000");
        assertThat(august.previousMonthRevenue()).isEqualByComparingTo("100000");
        assertThat(sales.getFirst().previousMonthRevenue()).isNull();
    }

    @Test
    @DisplayName("카테고리별 1위: 프로그래밍은 jOOQ 책, 데이터베이스는 공동 1위 2권")
    void bestSellers() {
        List<BestSeller> top1 = statsRepository.bestSellersByCategory(1);

        assertThat(top1).filteredOn(b -> b.categoryName().equals("프로그래밍"))
                .extracting(BestSeller::title)
                .containsExactly("jOOQ로 배우는 타입 안전 SQL");
        assertThat(top1).filteredOn(b -> b.categoryName().equals("데이터베이스"))
                .hasSize(2)
                .allSatisfy(b -> assertThat(b.rank()).isEqualTo(1));
    }

    @Test
    @DisplayName("재귀 CTE는 깊이와 경로를 계산한다")
    void categoryTree() {
        List<CategoryNode> tree = categoryRepository.findTree();

        assertThat(tree).filteredOn(n -> n.depth() == 0)
                .extracting(CategoryNode::name)
                .containsExactlyInAnyOrder("국내도서", "외국도서");
        assertThat(tree).filteredOn(n -> n.name().equals("프로그래밍"))
                .singleElement()
                .satisfies(n -> {
                    assertThat(n.depth()).isEqualTo(2);
                    assertThat(n.path()).isEqualTo("국내도서 > IT > 프로그래밍");
                });
    }
}
```

### 12-6. API 테스트 (MockMvc)

`src/test/java/com/example/bookstore/book/BookApiTest.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.AUTHOR;
import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookstore.TestcontainersConfiguration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BookApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DSLContext dsl;

    @Test
    @DisplayName("태그로 검색하면 배열 포함 조건(@>)으로 걸러진다")
    void searchByTag() throws Exception {
        mockMvc.perform(get("/api/books/search").param("tag", "jooq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("jOOQ로 배우는 타입 안전 SQL"));
    }

    @Test
    @DisplayName("도서를 등록하면 MULTISET 상세가 반환되고, 같은 ISBN 은 409")
    void createBook() throws Exception {
        long categoryId = dsl.select(CATEGORY.ID).from(CATEGORY)
                .where(CATEGORY.NAME.eq("프로그래밍")).fetchSingle().value1();
        long authorId = dsl.select(AUTHOR.ID).from(AUTHOR)
                .where(AUTHOR.NAME.eq("김지훈")).fetchSingle().value1();

        String body = """
                {
                  "categoryId": %d,
                  "title": "API 테스트 도서",
                  "isbn": "9799000000001",
                  "price": 12000,
                  "stock": 3,
                  "tags": ["testing"],
                  "attributes": {"language": "ko"},
                  "authorIds": [%d]
                }
                """.formatted(categoryId, authorId);

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.authors[0].name").value("김지훈"))
                .andExpect(jsonPath("$.category.name").value("프로그래밍"))
                .andExpect(jsonPath("$.attributes.language").value("ko"))
                .andExpect(jsonPath("$.attributes.pages").doesNotExist());

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("잘못된 ISBN 은 400 과 필드별 오류를 돌려준다")
    void validationError() throws Exception {
        String body = """
                {"categoryId": 1, "title": "x", "isbn": "123", "price": 1000, "stock": 1, "authorIds": [1]}
                """;

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.isbn").exists());
    }
}
```

### 12-7. DB 없는 단위 테스트: SQL 렌더링 검사

`src/test/java/com/example/bookstore/book/QueryRenderingTest.java`

```java
package com.example.bookstore.book;

import static com.example.bookstore.jooq.Tables.BOOK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.row;

import java.math.BigDecimal;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Spring 컨텍스트와 DB 없이 jOOQ 가 만드는 SQL 만 검사한다. (수 밀리초) */
class QueryRenderingTest {

    private final DSLContext dsl = DSL.using(SQLDialect.POSTGRES);

    @Test
    @DisplayName("키셋 조건은 row value 비교로 렌더링된다")
    void keysetCondition() {
        String sql = dsl.renderInlined(
                dsl.select(BOOK.ID)
                        .from(BOOK)
                        .where(row(BOOK.PRICE, BOOK.ID).gt(new BigDecimal("22000"), 4L)));

        assertThat(sql).contains("> (").contains("22000");
    }

    @Test
    @DisplayName("조건이 없으면 WHERE 절이 렌더링되지 않는다")
    void noCondition() {
        String sql = dsl.render(dsl.select(BOOK.ID).from(BOOK).where(DSL.noCondition()));

        assertThat(sql).doesNotContain("where");
    }
}
```

### 실행 확인

```bash
./gradlew test
# BUILD SUCCESSFUL — 최초 1회는 postgres:18-alpine 이미지를 내려받아 조금 오래 걸립니다.

./gradlew bootTestRun
# 로컬 DB 없이 Testcontainers 로 앱을 실행 (compose.yaml 불필요)
```

테스트 리포트는 `build/reports/tests/test/index.html` 에 생깁니다.

### 연습 과제

1. 8장 연습 과제 2처럼 `decreaseStock` 을 "읽고-확인하고-쓰기" 방식으로 바꾸고 `concurrentOrdersNeverOversell` 이 실패하는 것을 확인한 뒤 되돌리세요.
2. `spring-boot-starter-jooq-test` 를 추가하고 `@JooqTest` 슬라이스로 `AuthorRepositoryTest` 를 다시 작성해 보세요. 슬라이스 테스트는 jOOQ·DataSource·Flyway만 띄우므로 더 빠르며, 리포지토리 빈은 `@Import(AuthorRepository.class)` 로 직접 등록해야 합니다.
3. `MemberRepository.upsert` 에 대해 "같은 이메일로 10개 스레드가 동시에 업서트해도 회원은 1명"임을 검증하는 테스트를 작성해 보세요.

**예상 결과**

1. `concurrentOrdersNeverOversell`이 "expected: 5 but was: N"(N은 5보다 크고 실행마다 다름) 같은 AssertionError로 실패합니다. 되돌리면 다시 통과합니다. "확인과 변경을 SQL 한 문장으로"가 왜 필요한지 테스트로 확인하는 과제입니다.
2. 테스트가 통과하고, 웹 계층을 띄우지 않아 전체 컨텍스트보다 빨리 끕납니다. `@Import(AuthorRepository.class)`를 빼면 `NoSuchBeanDefinitionException`이 납니다. DB도 필요하므로 `TestcontainersConfiguration`도 함께 `@Import`해야 합니다. `@JooqTest`는 테스트마다 트랜잭션을 롤백합니다.
3. 10개 스레드가 모두 같은 id를 돌려받고 회원 수는 1명 늘어납니다. `ON CONFLICT`가 원자적이라 UNIQUE 위반이 나지 않습니다. 스레드마다 별도 트랜잭션이어야 하므로 이 테스트에는 `@Transactional`을 붙이지 말고, 끝난 뒤 만든 회원을 직접 지워야 합니다.

## 13장. 마무리 — 최종 구조, 체크리스트, 다음 단계

12장까지 마쳤다면 스키마 3개, 생성 코드, REST API 30개, 통합 테스트를 갖춘 북스토어 주문 시스템이 완성된 것입니다. 이 장은 전체를 한눈에 점검하는 참고용입니다.

### 13-1. 최종 프로젝트 구조

```text
bookstore/
├── settings.gradle.kts                       (1장)
├── build.gradle.kts                          (1장)
├── compose.yaml                              (1장)
├── .gitignore                                (1장)
├── src/main/resources/
│   ├── application.yml                       (1장)
│   ├── static/index.html                     (1장, API 콘솔)
│   └── db/migration/
│       ├── V1__init_schema.sql               (2장)
│       ├── V2__seed_data.sql                 (2장)
│       └── V3__add_indexes.sql               (2장)
├── src/generated/java/com/example/bookstore/jooq/  (3장, jooqCodegen 생성물 — 커밋)
├── src/main/java/com/example/bookstore/
│   ├── BookstoreApplication.java             (1장)
│   ├── common/
│   │   ├── NotFoundException.java            (4장)
│   │   ├── ConflictException.java            (4장)
│   │   ├── GlobalExceptionHandler.java       (4장)
│   │   └── PageResponse.java                 (6장)
│   ├── config/
│   │   ├── SlowQueryListener.java            (11장)
│   │   └── JooqConfig.java                   (11장)
│   ├── author/
│   │   ├── Author.java, AuthorRequest.java   (4장)
│   │   ├── AuthorRepository.java             (4장)
│   │   └── AuthorController.java             (4장)
│   ├── book/
│   │   ├── BookAttributes.java, BookAttributesConverter.java       (3장)
│   │   ├── BookSummary.java, BookWithAuthors.java                  (5장)
│   │   ├── BookRepository.java, BookController.java                (5장)
│   │   ├── BookSearchCondition.java, BookSort.java, BookScrollResponse.java (6장)
│   │   ├── BookSearchRepository.java, BookSearchController.java    (6장)
│   │   ├── AuthorRef.java, CategoryRef.java, BookDetail.java       (7장)
│   │   ├── BookDetailRepository.java, BookDetailController.java    (7장)
│   │   ├── BookCreateRequest.java, PriceChangeRequest.java         (8장)
│   │   ├── BookCommandService.java, BookCommandController.java     (8장)
│   │   ├── TagCount.java                                           (9장)
│   │   └── BookAdminRepository.java, BookAdminController.java      (9장)
│   ├── order/
│   │   ├── PlaceOrderRequest.java, OrderLine.java, OrderDetail.java, OrderSummary.java (8장)
│   │   ├── OrderRepository.java, OrderService.java                 (8장)
│   │   └── OrderController.java                                    (8장)
│   ├── member/
│   │   ├── Member.java, MemberUpsertRequest.java                   (9장)
│   │   └── MemberRepository.java, MemberController.java            (9장)
│   ├── stats/
│   │   ├── MonthlySales.java, BestSeller.java                      (10장)
│   │   └── StatsRepository.java, StatsController.java              (10장)
│   └── category/
│       ├── CategoryNode.java                                       (10장)
│       └── CategoryRepository.java, CategoryController.java        (10장)
└── src/test/java/com/example/bookstore/
    ├── TestcontainersConfiguration.java, TestBookstoreApplication.java, BookstoreApplicationTests.java (12장)
    ├── author/AuthorRepositoryTest.java      (12장)
    ├── order/OrderServiceTest.java           (12장)
    ├── stats/StatsRepositoryTest.java        (12장)
    └── book/BookApiTest.java, QueryRenderingTest.java (12장)
```

### 13-2. 완성된 API 목록

아래 API는 모두 1-8의 API 콘솔(http://localhost:8080/) 왼쪽 목록에 장 순서대로 들어 있습니다.

| 메서드 | 경로 | 설명 | 장 |
| --- | --- | --- | --- |
| GET | `/api/authors?keyword=` | 저자 목록·검색 | 4 |
| GET | `/api/authors/{id}` | 저자 조회 | 4 |
| POST | `/api/authors` | 저자 등록 | 4 |
| PUT | `/api/authors/{id}` | 저자 수정 (DSL UPDATE) | 4 |
| PATCH | `/api/authors/{id}/name` | 이름 변경 (액티브 레코드) | 4 |
| DELETE | `/api/authors/{id}` | 저자 삭제 | 4 |
| GET | `/api/books/{id}` | 도서 요약 (명시적 조인) | 5 |
| GET | `/api/books?category=` | 카테고리별 도서 (path 조인) | 5 |
| GET | `/api/books/with-authors` | 도서 + 저자 이름 배열 | 5 |
| GET | `/api/books/search` | 동적 검색 + 오프셋 페이지 | 6 |
| GET | `/api/books/scroll` | 키셋 페이지네이션 | 6 |
| GET | `/api/books/{id}/detail` | 도서 상세 (MULTISET) | 7 |
| POST | `/api/books` | 도서 등록 (트랜잭션) | 8 |
| PATCH | `/api/books/{id}/price` | 가격 변경 (낙관적 락) | 8 |
| POST | `/api/orders` | 주문 (원자적 재고 차감) | 8 |
| GET | `/api/orders/{id}` | 주문 상세 (MULTISET) | 8 |
| GET | `/api/orders?memberId=` | 회원별 주문 (스칼라 서브쿼리) | 8 |
| POST | `/api/orders/{id}/pay` | 결제 (조건부 상태 전이) | 8 |
| POST | `/api/orders/{id}/cancel` | 취소 (FOR UPDATE + UPDATE FROM) | 8 |
| PUT | `/api/members` | 회원 업서트 (ON CONFLICT) | 9 |
| GET | `/api/members`, `/api/members/{id}` | 회원 조회 | 9 |
| POST / DELETE | `/api/books/{id}/tags/{tag}` | 태그 추가/제거 (배열 함수) | 9 |
| PATCH | `/api/books/{id}/attributes` | 속성 병합 (JSONB) | 9 |
| GET | `/api/tags/stats` | 태그 통계 (unnest) | 9 |
| GET | `/api/stats/monthly-sales` | 월별 매출 (윈도우 함수) | 10 |
| GET | `/api/stats/best-sellers?top=` | 카테고리별 순위 (CTE + rank) | 10 |
| GET | `/api/stats/order-status` | 상태별 건수 (enum) | 10 |
| GET | `/api/categories/tree` | 카테고리 트리 (재귀 CTE) | 10 |

### 13-3. 학습 점검 체크리스트

- [ ] 스키마를 바꾸는 절차(새 마이그레이션 → `jooqCodegen` → 컴파일 → 한 커밋)를 설명할 수 있다.
- [ ] `fetch`, `fetchOne`, `fetchOptional`, `fetchSingle` 을 상황에 맞게 고를 수 있다.
- [ ] `Records.mapping` 이 왜 `fetchInto(Class)` 보다 안전한지 설명할 수 있다.
- [ ] `noCondition()` 으로 동적 쿼리를 조립하고, 정렬을 화이트리스트(enum)로 제한할 수 있다.
- [ ] 오프셋과 키셋 페이지네이션의 차이와 키셋에 필요한 인덱스를 설명할 수 있다.
- [ ] MULTISET으로 N+1 없이 중첩 컬렉션을 읽고, 그 비용을 설명할 수 있다.
- [ ] 재고 차감처럼 경쟁이 있는 쓰기를 조건부 UPDATE로 해결할 수 있다.
- [ ] 낙관적 락과 `FOR UPDATE` 중 무엇을 쓸지 판단할 수 있다.
- [ ] Converter(전역)와 `convertFrom`(쿼리 단위)을 구분해 쓸 수 있다.
- [ ] Plain SQL 템플릿을 바인드 변수와 함께 안전하게 쓸 수 있다.
- [ ] 윈도우 함수, CTE, 재귀 CTE를 jOOQ DSL로 작성할 수 있다.
- [ ] 리스너를 빈으로 등록해야 하는 이유(예외 변환 유지)를 설명할 수 있다.
- [ ] Testcontainers로 롤백·동시성을 검증하는 테스트를 작성할 수 있다.

### 13-4. 자주 막히는 문제

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| `Unsupported Database: PostgreSQL 18` | `flyway-database-postgresql` 누락 | 의존성 추가 |
| 기동해도 Flyway가 실행되지 않음 | Boot 4에서 `flyway-core` 만 추가 | `spring-boot-starter-flyway` 로 교체 |
| 생성 코드 컴파일 에러 / 버전 경고 | 코드 생성기와 런타임 jOOQ 버전 불일치 | `extra["jooq.version"]` 과 플러그인 버전을 같게 |
| `DefaultConfigurationCustomizer` 를 찾을 수 없음 | Boot 3 패키지 경로 사용 | `org.springframework.boot.jooq.autoconfigure` 로 변경 |
| 중복 키인데 500 에러 | 커스터마이저에서 리스너를 교체해 예외 변환이 빠짐 | `ExecuteListenerProvider` 빈으로 등록 |
| 재귀 CTE "column has type ... overall" | 시작 항과 반복 항 타입 불일치 | 시작 항을 `cast` 로 맞춤 |
| PostgreSQL 18 컨테이너가 바로 종료 | 볼륨을 `/var/lib/postgresql/data` 에 마운트 | `/var/lib/postgresql` 로 변경 |
| 테스트에서 `Could not find a valid Docker environment` | Docker 미실행 | Docker Desktop 등 실행 |

### 13-5. 다음 단계

1. **코드 생성 고도화**: 커스텀 `GeneratorStrategy` 로 클래스 이름 규칙 바꾸기, embeddable(여러 컬럼을 하나의 값 객체로), `recordVersionFields` 로 레코드 낙관적 락 자동화.
2. **다른 생성 전략 비교**: 빌드 시 Testcontainers로 임시 DB를 띄워 코드를 생성하는 방식을 시도해 보고, "생성 코드 커밋" 방식과 장단점을 비교합니다.
3. **멀티테넌트·보안**: `VisitListener` 또는 jOOQ의 정책(Policy) 기능으로 모든 쿼리에 `tenant_id = ?` 를 자동으로 붙이는 방법(일부 기능은 상용 에디션 전용이므로 에디션을 확인).
4. **대량 처리**: `fetchLazy()`/`fetchStream()` 로 커서 기반 조회, `Loader` API와 `COPY` 로 대량 적재.
5. **비동기**: jOOQ는 R2DBC도 지원합니다. 다만 Spring Boot 4 + 가상 스레드 환경에서는 JDBC 블로킹 방식으로도 높은 동시성을 얻을 수 있으므로, 먼저 현재 구조에서 부하 테스트를 해 보는 것을 권합니다.
6. **Spring Boot 4.2 업그레이드 연습**: 4.2 GA가 나오면 플러그인 버전을 올리고 테스트가 모두 통과하는지 확인해 보세요. 이때 12장의 테스트가 안전망 역할을 합니다.

### 참고 자료

- [jOOQ 매뉴얼 — Gradle 코드 생성 (3.21)](https://www.jooq.org/doc/latest/manual/code-generation/codegen-gradle/)
- [jOOQ 3.21 What's new](https://www.jooq.org/doc/latest/manual/whats-new/)
- [jOOQ 릴리스 노트](https://jooq.org/notes)
- [jOOQ 지원 DB 버전 매트릭스](https://jooq.org/download/support-matrix)
- [Spring Boot 4.1.0 릴리스 발표](https://spring.io/blog/2026/06/10/spring-boot-4/)
- [Spring Boot GitHub Releases (4.1.2)](https://github.com/spring-projects/spring-boot/releases)
- [Spring Boot 4.0 Migration Guide (모듈화·스타터 변경)](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Flyway 12.11.0 릴리스 정보](https://www.componentsource.com/product/flyway/releases/2972431)
- [Testcontainers PostgreSQL 모듈](https://testcontainers.com/modules/postgresql/)
- [Gradle 9.6.1 릴리스](https://discuss.gradle.org/t/9-6-1/52110)
