-- PostgreSQL enum 타입은 jOOQ가 자동으로 Java enum으로 생성한다.
CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'SHIPPED', 'CANCELLED');

CREATE TABLE category (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES category (id),
    name VARCHAR (100) NOT NULL UNIQUE
);

CREATE TABLE author (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birth_year INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE book (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES category (id),
    title VARCHAR (200) NOT NULL,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    published_on DATE,
    tags TEXT[] NOT NULL DEFAULT '{}',
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE book_author (
    book_id BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES author (id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE member (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member (id),
    status order_status NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ordered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES book (id),
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    unit_price NUMERIC(10, 2) NOT NULL,
    UNIQUE (order_id, book_id)
);

