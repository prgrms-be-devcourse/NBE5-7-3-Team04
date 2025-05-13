package me.performancereservation.global.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthCheckController {

  @Value("${commit.hash}")
  private String commitHash;

  @ResponseBody
  @GetMapping("/health-check")
  public String healthCheck() {
    return commitHash;
  }

}
