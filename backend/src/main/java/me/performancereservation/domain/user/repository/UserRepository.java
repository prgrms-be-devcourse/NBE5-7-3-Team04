package me.performancereservation.domain.user.repository;

import me.performancereservation.domain.user.entitiy.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); //이메일 중복 체크
}
