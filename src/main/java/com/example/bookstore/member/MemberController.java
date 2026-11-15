package com.example.bookstore.member;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.common.NotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberRepository memberRepository;

	public MemberController(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	/** PUT은 멱등하므로 업서트에 잘 맞는다. */
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
