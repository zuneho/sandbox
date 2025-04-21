package io.github.zuneho.auth.service;


import io.github.zuneho.auth.model.LoginRequest;
import io.github.zuneho.auth.model.MemberDto;
import io.github.zuneho.auth.model.TokenResponse;
import io.github.zuneho.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService  {

    private final MemberService memberService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional
    public Member signup(MemberDto memberDto) {
        return memberService.register(memberDto);
    }

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        // 실제 인증 진행 (UserDetailsService의 loadUserByUsername 호출)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성
        String jwt = tokenProvider.createToken(authentication);

        return TokenResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24시간 (밀리초)
                .build();
    }
}
