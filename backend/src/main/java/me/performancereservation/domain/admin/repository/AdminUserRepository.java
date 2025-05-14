package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.user.entitiy.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<User, Long> {
}
