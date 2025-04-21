package io.github.zuneho.auth.model;


import java.util.Map;

public class LineOAuth2UserInfo extends OAuth2UserInfo {

    public LineOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("userId");
    }

    @Override
    public String getName() {
        return (String) attributes.get("displayName");
    }

    @Override
    public String getEmail() {
        // LINE API는 기본적으로 이메일을 제공하지 않을 수 있음
        // email scope가 있는 경우에만 제공됨
        return (String) attributes.getOrDefault("email", "");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("pictureUrl");
    }

    @Override
    public String getNameAttributeKey() {
        return "userId";
    }
}