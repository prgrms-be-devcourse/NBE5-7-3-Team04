package me.performancereservation.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("Authorization")

        val securityRequirement = SecurityRequirement().addList("Authorization")

        return OpenAPI()
            .components(Components())
            .info(apiInfo())
            .addSecurityItem(securityRequirement)
            .schemaRequirement("Authorization", securityScheme)
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Ticket4U API")
            .description("공연 예약 서비스 'Ticket4U' api 문서입니다.")
            .version("1.0.0")
    }
}