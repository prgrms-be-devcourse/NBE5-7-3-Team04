package me.performancereservation.api

import me.performancereservation.domain.admin.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/user")
class AdminUserController(
    private val adminService: AdminService
) {
    @GetMapping("/{userId}")
    fun getUser(@PathVariable("userId") userId: Long): ResponseEntity<String> {
        return ResponseEntity.ok(adminService.getUserName(userId))
    }
}