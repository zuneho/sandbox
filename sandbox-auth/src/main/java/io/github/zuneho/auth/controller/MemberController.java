package io.github.zuneho.auth.controller;


import io.github.zuneho.auth.service.MemberService;
import io.github.zuneho.auth.model.MemberDto;
import io.github.zuneho.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        // 인증 객체에서 식별자 추출
        String identifier = authentication.getName();
        Member member = memberService.findByOauthKeyOrEmail(identifier)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + identifier));

        // 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환
        MemberDto memberDto = MemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .profileImageUrl(member.getProfileImageUrl())
                .oauthPlatform(member.getOauthPlatform())
                .build();

        return ResponseEntity.ok(memberDto);
    }
}
