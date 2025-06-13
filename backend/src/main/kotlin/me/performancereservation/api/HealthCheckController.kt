package me.performancereservation.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HealthCheckController {
    @Value("\${commit.hash}")
    private val commitHash: String? = null

    @ResponseBody
    @GetMapping("/health-check")
    fun healthCheck(): String? {
        return commitHash
    }
}