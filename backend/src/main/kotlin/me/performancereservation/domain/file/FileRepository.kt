package me.performancereservation.domain.file

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FileRepository : JpaRepository<File, Long> {
    @Query("""
        SELECT f 
        FROM File f 
        WHERE f.id IN :fileIds
    """)
    fun findAllById(@Param("fileIds") fileIds: List<Long>): List<File>
}
