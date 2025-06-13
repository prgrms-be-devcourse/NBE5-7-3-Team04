package me.performancereservation.domain.bookmark

import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse
import me.performancereservation.domain.file.FileRepository
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val fileRepository: FileRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val performanceRepository: PerformanceRepository
) {

    /**
     * 사용자의 id와 공연 id를 이용해 찜 생성
     *
     * @param userId 사용자 ID
     * @param performanceId 공연 ID
     */
    @Transactional
    fun performanceBookmark(userId: Long, performanceId: Long) {
        // 공연 존재 여부 확인
        performanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        // 중복 찜 확인
        require(!bookmarkRepository.existsByUserIdAndPerformanceId(userId, performanceId)) {
            throw ErrorCode.DUPLICATED_BOOKMARK.domainException("이미 같은 공연에 찜이 존재 합니다.")
        }

        // 찜 생성 및 저장
        val bookmark = Bookmark(userId = userId, performanceId = performanceId)
        bookmarkRepository.save(bookmark)
    }

    /**
     * 사용자의 id와 공연 id를 받아 찜 취소
     *
     * @param userId 사용자 ID
     * @param performanceId 공연 ID
     */
    @Transactional
    fun performanceBookmarkCancel(userId: Long, performanceId: Long) {
        // 공연 존재 여부 확인
        performanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        // 북마크 존재 여부 확인 및 삭제
        val bookmark = bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId)
            ?: throw ErrorCode.BOOKMARK_NOT_FOUND.domainException("이미 찜이 안되어 있는 공연입니다.")

        bookmarkRepository.delete(bookmark)
    }

    /**
     * 사용자의 id를 받아 찜한 공연 목록을 페이징으로 반환
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 북마크된 공연 목록 페이지
     */
    @Transactional(readOnly = true)
    fun getBookmarkedPerformances(userId: Long, pageable: Pageable): Page<BookmarkedPerformancePageResponse> {
        // 페이징된 북마크 목록 조회
        val bookmarks = bookmarkRepository.findAllByUserId(userId, pageable)

        // 북마크된 공연 ID 목록 추출
        val performanceIds = bookmarks.content.map { it.performanceId }

        // 공연 ID에 해당하는 공연 목록 조회 및 매핑
        val performanceMap = performanceRepository.findAllById(performanceIds)
            .associateBy { it.id!! }

        // 파일 ID 추출 및 파일 URL 매핑
        val fileIds = performanceMap.values.mapNotNull { it.fileId }
        val fileUrlMap = fileRepository.findAllById(fileIds)
            .associate { it.id to it.key }

        // 북마크 순서를 유지하면서 응답 객체 생성
        return bookmarks.map { bookmark ->
            convertToBookmarkedPerformanceResponse(bookmark, performanceMap, fileUrlMap)
        }
    }

    /** 북마크 정보를 이용해 북마크된 공연 응답 객체로 변환
     *
     * @param bookmark 북마크 객체
     * @param performanceMap 공연 ID로 조회한 공연 매핑
     * @param fileUrlMap 파일 ID로 조회한 파일 URL 매핑
     * @return 북마크된 공연 페이지 응답 객체
     */
    private fun convertToBookmarkedPerformanceResponse(
        bookmark: Bookmark,
        performanceMap: Map<Long, Performance>,
        fileUrlMap: Map<Long, String>
    ): BookmarkedPerformancePageResponse {
        val performance = performanceMap[bookmark.performanceId]!!

        return BookmarkedPerformancePageResponse(
            id = performance.id!!,
            fileUrl = performance.fileId?.let { fileUrlMap[it] },
            title = performance.title,
            price = performance.price,
            startDate = performance.startDate,
            endDate = performance.endDate,
            venue = performance.venue,
            category = performance.category,
            status = performance.status,
            bookmarked = true // 북마크 목록이므로 모두 북마크되어 있음
        )
    }
}