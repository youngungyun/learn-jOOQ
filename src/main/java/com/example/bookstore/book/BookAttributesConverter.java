package com.example.bookstore.book;

import org.jooq.Converter;
import org.jooq.JSONB;
import org.jspecify.annotations.Nullable;

import tools.jackson.databind.json.JsonMapper;

/**
 * jOOQ Converter: DB 타입(JSONB) <-> 사용자 타입(BookAttributes)
 * 생성 코드가 new BookAttributesConverter()로 인스턴스를 만들기 떄문에
 * public 기본 생성자가 반드시 있어야 한다.
 */
public class BookAttributesConverter implements Converter<JSONB, BookAttributes> {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	public BookAttributesConverter() {}

	@Override
	public BookAttributes from(@Nullable JSONB databaseObject) {
		if (databaseObject == null) {
			return null;
		}
		return MAPPER.readValue(databaseObject.data(), BookAttributes.class);
	}

	@Override
	public JSONB to(@Nullable BookAttributes userObject) {
		if (userObject == null) {
			return  null;
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
