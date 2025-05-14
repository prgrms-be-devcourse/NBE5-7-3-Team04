package me.performancereservation.domain.bookmark;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor
public class Bookmark extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 찜 ID

    private Long userId;         // (FK) 유저 ID

    private Long performanceId;  // (FK) 공연 ID

    @Builder
    public Bookmark(Long id, Long performanceId, Long userId) {
        this.id = id;
        this.performanceId = performanceId;
        this.userId = userId;
    }
}
