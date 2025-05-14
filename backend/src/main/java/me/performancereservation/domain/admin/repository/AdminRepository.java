package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
}
