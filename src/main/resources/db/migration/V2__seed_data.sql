-- 카테고리 (3단 계층)
INSERT INTO category (name, parent_id)
VALUES ('국내도서', NULL),
       ('외국도서', NULL);
INSERT INTO category (name, parent_id)
VALUES ('소설', (SELECT id FROM category WHERE name = '국내도서')),
       ('IT', (SELECT id FROM category WHERE name = '국내도서')),
       ('Fiction', (SELECT id FROM category WHERE name = '외국도서'));
INSERT INTO category (name, parent_id)
VALUES ('프로그래밍', (SELECT id FROM category WHERE name = 'IT')),
       ('데이터베이스', (SELECT id FROM category WHERE name = 'IT'));

-- 저자
INSERT INTO author (name, birth_year)
VALUES ('김지훈', 1982),
       ('이서연', 1990),
       ('박민준', 1975),
       ('최유진', 1988),
       ('Emily Carter', 1979);

-- 도서
INSERT INTO book (category_id, title, isbn, price, stock, published_on, tags, attributes)
VALUES ((SELECT id FROM category WHERE name = '프로그래밍'), '모던 자바 실전', '9791100000011', 32000, 50, '2025-03-10',
        '{java,backend}', '{
    "language": "ko",
    "pages": 520,
    "format": "PAPERBACK"
  }'),
       ((SELECT id FROM category WHERE name = '프로그래밍'), '스프링 부트 4 완벽 가이드', '9791100000028', 38000, 30, '2026-01-15',
        '{java,spring,backend}', '{
         "language": "ko",
         "pages": 640,
         "format": "PAPERBACK"
       }'),
       ((SELECT id FROM category WHERE name = '데이터베이스'), 'PostgreSQL 운영과 튜닝', '9791100000035', 35000, 20, '2025-08-01',
        '{database,postgresql}', '{
         "language": "ko",
         "pages": 480,
         "format": "PAPERBACK"
       }'),
       ((SELECT id FROM category WHERE name = '데이터베이스'), 'SQL 첫걸음 다시 보기', '9791100000042', 22000, 100, '2024-11-20',
        '{database,sql,beginner}', '{
         "language": "ko",
         "pages": 300,
         "format": "EBOOK"
       }'),
       ((SELECT id FROM category WHERE name = '소설'), '새벽의 정원', '9791100000059', 15000, 70, '2025-05-05',
        '{novel,bestseller}', '{
         "language": "ko",
         "pages": 280,
         "format": "PAPERBACK"
       }'),
       ((SELECT id FROM category WHERE name = '소설'), '바다로 가는 기차', '9791100000066', 14500, 0, '2023-09-09',
        '{novel}', '{
         "language": "ko",
         "pages": 250,
         "format": "PAPERBACK"
       }'),
       ((SELECT id FROM category WHERE name = 'Fiction'), 'The Quiet Algorithm', '9781000000017', 27000, 15,
        '2025-10-01',
        '{novel,english}', '{
         "language": "en",
         "pages": 350,
         "format": "HARDCOVER"
       }'),
       ((SELECT id FROM category WHERE name = '프로그래밍'), 'jOOQ로 배우는 타입 안전 SQL', '9791100000073', 30000, 40, '2026-06-30',
        '{java,jooq,database}', '{
         "language": "ko",
         "pages": 420,
         "format": "PAPERBACK"
       }');

-- 도서-저자 연결 (공저 포함)
INSERT INTO book_author (book_id, author_id)
SELECT b.id, a.id
FROM (VALUES ('9791100000011', '김지훈'),
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
INSERT INTO member (email, name)
VALUES ('minsu@example.com', '김민수'),
       ('jiyoung@example.com', '이지영'),
       ('hana@example.com', '박하나');

-- 주문: 데이터 변경 CTE(INSERT ... RETURNING)로 주문과 항목을 한 번에 넣는다
WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'minsu@example.com'), 'PAID', 70000, '2026-07-03 10:00:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000011', 1), ('9791100000028', 1)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'SHIPPED', 30000, '2026-07-15 14:30:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000059', 2)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'hana@example.com'), 'PAID', 57000, '2026-08-02 09:15:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000035', 1), ('9791100000042', 1)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'minsu@example.com'), 'SHIPPED', 60000, '2026-08-20 20:00:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000073', 2)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'CANCELLED', 27000, '2026-09-05 11:00:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9781000000017', 1)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'hana@example.com'), 'PENDING', 53000, '2026-09-28 18:45:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000059', 1), ('9791100000028', 1)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;

WITH o AS (
INSERT
INTO orders (member_id, status, total_amount, ordered_at)
VALUES ((SELECT id FROM member WHERE email = 'jiyoung@example.com'), 'PAID', 62000, '2026-10-10 13:20:00+09')
    RETURNING id)
INSERT
INTO order_item (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, v.qty, b.price
FROM o,
     (VALUES ('9791100000073', 1), ('9791100000011', 1)) AS v(isbn, qty)
         JOIN book b ON b.isbn = v.isbn;