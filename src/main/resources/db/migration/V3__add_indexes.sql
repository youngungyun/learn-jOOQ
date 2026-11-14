-- FK 조인·필터용
CREATE INDEX idx_category_parent_id ON category (parent_id);
CREATE INDEX idx_book_category_id ON book (category_id);
CREATE INDEX idx_book_author_author_id ON book_author (author_id);
CREATE INDEX idx_order_item_book_id ON order_item (book_id);

-- 회원별 최근 주문 조회용
CREATE INDEX idx_orders_member_id_ordered_at ON orders (member_id, ordered_at DESC);

-- 키셋 페이지네이션(가격순) 용 복합 인덱스 (6장)
CREATE INDEX idx_book_price_id ON book (price, id);

-- 배열 포함 연산자 (@>) 용 GIN 인덱스 (6장)
CREATE INDEX idx_book_tags ON book USING GIN (tags);