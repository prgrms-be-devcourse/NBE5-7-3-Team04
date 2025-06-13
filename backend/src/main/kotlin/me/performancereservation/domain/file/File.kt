package me.performancereservation.domain.file

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity

@Entity
class File (
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long? = null, // 파일 ID

    @Column(name = "`key`")
    val key: String // Storage Key
) : BaseEntity()
