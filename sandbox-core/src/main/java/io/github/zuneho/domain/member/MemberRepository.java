package io.github.zuneho.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByOauthKeyAndOauthPlatform(String oauthKey, String oauthPlatform);
    boolean existsByEmail(String email);
    Optional<Member> findByOauthKey(String oauthKey);
}
