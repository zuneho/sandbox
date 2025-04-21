package io.github.zuneho.auth.service;




import io.github.zuneho.auth.model.*;
import io.github.zuneho.domain.member.Member;
import io.github.zuneho.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 예외 발생", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User);

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("OAuth2 공급자로부터 이메일을 찾을 수 없습니다.");
        }

        Optional<Member> memberOptional = memberRepository.findByOauthKeyAndOauthPlatform(
                oAuth2UserInfo.getId(), registrationId);

        Member member;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
            // 기존 회원 정보 업데이트
            member = updateExistingMember(member, oAuth2UserInfo);
        } else {
            // 새 회원 등록
            member = registerNewMember(oAuth2UserRequest, oAuth2UserInfo);
        }

        return new CustomOAuth2User(
                member.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()),
                oAuth2User.getAttributes(),
                oAuth2UserInfo.getNameAttributeKey(),
                member.getEmail(),
                member.getOauthPlatform()
        );
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, OAuth2User oAuth2User) {
        switch(registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            case "line":
                return new LineOAuth2UserInfo(oAuth2User.getAttributes());
            case "wechat":
                return new WeChatOAuth2UserInfo(oAuth2User.getAttributes());
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 공급자입니다: " + registrationId);
        }
    }

    private Member registerNewMember(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        Member member = Member.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .oauthKey(oAuth2UserInfo.getId())
                .oauthPlatform(registrationId)
                .profileImageUrl(oAuth2UserInfo.getImageUrl())
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        return memberRepository.save(member);
    }

    private Member updateExistingMember(Member existingMember, OAuth2UserInfo oAuth2UserInfo) {
        existingMember.setName(oAuth2UserInfo.getName());
        existingMember.setProfileImageUrl(oAuth2UserInfo.getImageUrl());

        return memberRepository.save(existingMember);
    }
}