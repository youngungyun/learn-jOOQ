package com.example.bookstore.member;

import static com.example.bookstore.jooq.Tables.*;
import static org.jooq.impl.DSL.excluded;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.bookstore.jooq.tables.records.MemberRecord;

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
	 * "조회 후 없으면 INSERT"와 달리 동시 요청에서도 UNIQUE 위반이 나지 않는다.
	 */
	public Member upsert(String email, String name) {
		var record = dsl.insertInto(MEMBER)
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
