package me.performancereservation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest;
import me.performancereservation.domain.review.repository.ReviewRepository;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ObjectMapper objectMapper;

    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder()
                        .name("테스트유저")
                        .email("test@email.com")
                        .phoneNumber("010-1234-5678")
                        .role(Role.USER)
                        .build()
        );
    }

    @Test
    void 리뷰_작성_성공() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(1L, 1L, "재밌었어요!");
        CustomOAuth2User principal = new CustomOAuth2User(user, Map.of());

        // when & then
        mockMvc.perform(post("/api/v1/review")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰가 성공적으로 작성되었습니다."));

        // 실제 DB에 저장되었는지 검증
        assertThat(reviewRepository.findAll()).anyMatch(r -> r.getComments().equals("재밌었어요!"));
    }
}