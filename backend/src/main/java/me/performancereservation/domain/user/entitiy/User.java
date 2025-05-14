package me.performancereservation.domain.user.entitiy;

import jakarta.persistence.*;
import lombok.*;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.user.enums.Role;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    //User는 유저 정보만 가진다
    //인증, 로그인 정보는 Auth에서 관리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 유저 ID

    private String email; // 유저 Email

    @Column(nullable = true)
    private String name; // 유저 이름

    @Column(nullable = true)
    private String phoneNumber; // 휴대폰번호

    @Enumerated(EnumType.STRING)
    private Role role; // 권한

    @Builder
    public User(Long id, String email, String name, String phoneNumber, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

}

