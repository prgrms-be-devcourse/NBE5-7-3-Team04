package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping("/{userId}")
    public ResponseEntity<String> getUser(@PathVariable("userId") long userId) {
        return ResponseEntity.ok(adminService.getUserName(userId));
    }

}
