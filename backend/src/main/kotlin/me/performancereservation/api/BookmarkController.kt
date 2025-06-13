package me.performancereservation.api

import me.performancereservation.api.docs.BookmarkApiDocs
import me.performancereservation.domain.bookmark.BookmarkService
import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bookmark")
class BookmarkController(
    private val bookmarkService: BookmarkService
) : BookmarkApiDocs {

    //북마크 하기
    @PostMapping("/{performanceId}")
    override fun performanceBookmark(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable performanceId: Long
    ): ResponseEntity<Void> {
        bookmarkService.performanceBookmark(principal.getUser().id!!, performanceId)

        return ResponseEntity.noContent().build()
    }

    //북마크 취소
    @PatchMapping("/{performanceId}")
    override fun performanceBookmarkCancel(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable performanceId: Long
    ): ResponseEntity<Void> {
        bookmarkService.performanceBookmarkCancel(principal.getUser().id!!, performanceId)

        return ResponseEntity.noContent().build()
    }

    //북마크 공연 목록 조회
    @GetMapping
    override fun getBookmarkedPerformances(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        pageable: Pageable
    ): ResponseEntity<Page<BookmarkedPerformancePageResponse>> {
        val bookmarkedPerformances: Page<BookmarkedPerformancePageResponse> =
            bookmarkService.getBookmarkedPerformances(
                principal.getUser().id!!, pageable
            )
        return ResponseEntity.ok(bookmarkedPerformances)
    }
}