package me.performancereservation.domain.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auth ID

    //User와의 관계는 FK(userId)만 필드로 보유(JPA 연관관계 X, N+1 방지, 쿼리 명확화)
    private Long userId; // (FK) 유저 ID

    private String provider; // 써드파티 제공자

    private String oauthId; // 써드파티 식별자

    @Builder
    public Auth(Long id, Long userId, String provider, String oauthId) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.oauthId = oauthId;
    }
}
