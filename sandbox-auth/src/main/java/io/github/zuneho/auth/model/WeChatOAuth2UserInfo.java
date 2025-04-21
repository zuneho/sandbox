package io.github.zuneho.auth.model;


import java.util.Map;

public class WeChatOAuth2UserInfo extends OAuth2UserInfo {

    public WeChatOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("openid");
    }

    @Override
    public String getName() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getEmail() {
        // WeChat API는 기본적으로 이메일을 제공하지 않음
        // 다른 방법으로 사용자 식별 필요
        return (String) attributes.getOrDefault("unionid", "") + "@wechat.com";
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("headimgurl");
    }

    @Override
    public String getNameAttributeKey() {
        return "openid";
    }
}
