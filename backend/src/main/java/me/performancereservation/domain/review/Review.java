package me.performancereservation.domain.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    //리뷰 아이디, 유저 아이디, 공연 아이디, 회차, 리뷰 내용

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceId;

    private Long userId;

    @Column(nullable = false, length = 1000)
    private String comments; //또는 text 형식?

    @Builder
    public Review(Long userId, Long performanceId, String comments) {
        this.performanceId = performanceId;
        this.userId = userId;
        this.comments = comments;
    }

    public void updateComments(String comments) {
        this.comments = comments;
    }
}
