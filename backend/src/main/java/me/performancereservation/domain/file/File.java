package me.performancereservation.domain.file;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;

@Entity
@NoArgsConstructor
@Getter
public class File extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 파일 ID

    @Column(name = "`key`")
    private String key; // Storage Key

    @Builder
    public File(Long id, String key) {
        this.id = id;
        this.key = key;
    }
}
