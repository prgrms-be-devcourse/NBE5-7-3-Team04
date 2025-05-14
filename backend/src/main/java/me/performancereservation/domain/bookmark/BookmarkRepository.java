package me.performancereservation.domain.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndPerformanceId(Long userId, Long performanceId);
    Optional<Bookmark> findByUserIdAndPerformanceId(Long userId, Long performanceId);
    Page<Bookmark> findAllByUserId(Long userId, Pageable pageable);
}
