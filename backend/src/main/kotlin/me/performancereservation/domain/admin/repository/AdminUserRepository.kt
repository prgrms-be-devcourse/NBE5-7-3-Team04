package me.performancereservation.domain.admin.repository

import me.performancereservation.domain.user.entitiy.User
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<User, Long>
