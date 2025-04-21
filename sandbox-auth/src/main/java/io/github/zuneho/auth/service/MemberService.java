package io.github.zuneho.auth.service;

import io.github.zuneho.auth.model.CustomUserDetails;
import io.github.zuneho.auth.model.MemberDto;
import io.github.zuneho.domain.member.Member;
import io.github.zuneho.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new CustomUserDetails(member);
    }

    @Transactional
    public Member register(MemberDto memberDto) {
        if (memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(memberDto.getEmail())
                .password(memberDto.getPassword() != null ? passwordEncoder.encode(memberDto.getPassword()) : null)
                .name(memberDto.getName())
                .oauthKey(memberDto.getOauthKey())
                .oauthPlatform(memberDto.getOauthPlatform())
                .profileImageUrl(memberDto.getProfileImageUrl())
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByOAuthInfo(String oauthKey, String oauthPlatform) {
        return memberRepository.findByOauthKeyAndOauthPlatform(oauthKey, oauthPlatform);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByOauthKeyOrEmail(String identifier) {
        // 먼저 이메일로 검색
        Optional<Member> memberByEmail = memberRepository.findByEmail(identifier);
        if (memberByEmail.isPresent()) {
            return memberByEmail;
        }

        // 이메일로 찾지 못하면 OAuth 키로 검색
        return memberRepository.findByOauthKey(identifier);
    }
}