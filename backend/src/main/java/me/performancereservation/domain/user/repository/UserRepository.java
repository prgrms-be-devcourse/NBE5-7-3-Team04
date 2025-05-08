package me.performancereservation.domain.user.repository;

import me.performancereservation.domain.user.entitiy.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); //이메일 중복 체크
    Optional<User> findByEmail(String email); //이메일로 유저 찾기
}
