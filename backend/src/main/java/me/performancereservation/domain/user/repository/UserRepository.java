package me.performancereservation.domain.user.repository;

import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); //이메일 중복 체크
    Optional<User> findByEmail(String email); //이메일로 유저 찾기

    // // TODO: 테스트 완료 후 삭제
    // @Modifying
    // @Transactional
    // @Query("update User u set u.role = :role where u.id = :userId")
    // int updateUserRole(@Param("userId") Long userId, @Param("role") Role role);

}
