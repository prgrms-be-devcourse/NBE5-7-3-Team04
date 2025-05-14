package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.bookmark.BookmarkService;
import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformancePageResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    //북마크 하기
    @PostMapping("/{performanceId}")
    public ResponseEntity<Void> performanceBookmark(@AuthenticationPrincipal CustomOAuth2User principal,
                                                    @PathVariable Long performanceId) {
        bookmarkService.performanceBookmark(principal.getUser().getId(), performanceId);
        return ResponseEntity.noContent().build();
    }

    //북마크 취소
    @PatchMapping("/{performanceId}")
    public ResponseEntity<Void> performanceBookmarkCancel(@AuthenticationPrincipal CustomOAuth2User principal,
                                                          @PathVariable Long performanceId) {
        bookmarkService.performanceBookmarkCancel(principal.getUser().getId(), performanceId);
        return ResponseEntity.noContent().build();
    }

    //북마크 공연 목록 조회
    @GetMapping
    public ResponseEntity<Page<BookmarkedPerformancePageResponse>> getBookmarkedPerformances(@AuthenticationPrincipal CustomOAuth2User principal,
                                                                                             Pageable pageable) {
        Page<BookmarkedPerformancePageResponse> bookmarkedPerformances = bookmarkService.getBookmarkedPerformances(principal.getUser().getId(), pageable);
        return ResponseEntity.ok(bookmarkedPerformances);
    }
}
