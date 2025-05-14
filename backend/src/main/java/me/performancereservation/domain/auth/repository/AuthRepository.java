package me.performancereservation.domain.auth.repository;

import me.performancereservation.domain.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByProviderAndOauthId(String provider, String oauthId); //소셜로그인한 유저를 조회
}
