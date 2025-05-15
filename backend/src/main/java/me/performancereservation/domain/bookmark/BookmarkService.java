package me.performancereservation.domain.bookmark;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse;
import me.performancereservation.domain.file.File;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.dto.performance.response.PerformancePageResponse;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final FileRepository fileRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PerformanceRepository performanceRepository;

    /** 사용자의 id와 공연 id를 이용해 찜 생성
     *
     * @param userId
     * @param performanceId
     */
    @Transactional
    public void performanceBookmark(Long userId, Long performanceId) {
        performanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        // 찜이 안되어 있는지 확인
        if(bookmarkRepository.existsByUserIdAndPerformanceId(userId, performanceId)) {
            throw ErrorCode.DUPLICATED_BOOKMARK.domainException("이미 같은 공연에 찜이 존재 합니다.");
        }

        // 찜 생성
        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .performanceId(performanceId)
                .build();

        // 찜 저장
        bookmarkRepository.save(bookmark);
    }

    /** 사용자의 id와 공연 id를 받아 찜 취소
     *
     * @param userId
     * @param performanceId
     */
    @Transactional
    public void performanceBookmarkCancel(Long userId, Long performanceId) {
        performanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        Bookmark bookmark = bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId)
                .orElseThrow(() -> ErrorCode.BOOKMARK_NOT_FOUND.domainException("이미 찜이 안되어 있는 공연입니다."));

        // 찜 삭제
        bookmarkRepository.delete(bookmark);
    }

    /** 사용자의 id를 받아 찜한 공연 목록을 페이징으로 반환
     *
     * @param userId
     * @param pageable
     * @return Page<BookmarkedPerformancePageResponse>
     */
    @Transactional(readOnly = true)
    public Page<BookmarkedPerformancePageResponse> getBookmarkedPerformances(Long userId, Pageable pageable) {
        // 페이징된 북마크 목록 조회
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByUserId(userId, pageable);

        // 북마크된 공연 ID 목록 추출
        List<Long> performanceIds = bookmarks.getContent().stream()
                .map(Bookmark::getPerformanceId)
                .toList();

        // 공연 ID에 해당하는 공연 목록 조회
        List<Performance> performances = performanceRepository.findAllById(performanceIds);

        // 공연 ID로 조회한 공연 매핑
        Map<Long, Performance> performanceMap = performances.stream()
                .collect(Collectors.toMap(Performance::getId, performance -> performance));

        // 파일 ID 추출
        List<Long> fileIds = performances.stream()
                .map(Performance::getFileId)
                .filter(Objects::nonNull)
                .toList();

        // 파일 ID로 조회한 파일 URL 매핑
        Map<Long, String> fileUrlMap = fileRepository.findAllById(fileIds).stream()
                .collect(Collectors.toMap(File::getId, File::getKey));

        // 북마크 순서를 유지하면서 응답 객체 생성
        return bookmarks.map(bookmark -> convertToBookmarkedPerformanceResponse(bookmark, performanceMap, fileUrlMap));
    }

    /** 북마크 정보를 이용해 북마크된 공연 응답 객체로 변환
     *
     * @param bookmark 북마크 객체
     * @param performanceMap 공연 ID로 조회한 공연 매핑
     * @param fileUrlMap 파일 ID로 조회한 파일 URL 매핑
     * @return 북마크된 공연 페이지 응답 객체, 없는 경우 null
     */
    private BookmarkedPerformancePageResponse convertToBookmarkedPerformanceResponse(
            Bookmark bookmark,
            Map<Long, Performance> performanceMap,
            Map<Long, String> fileUrlMap) {

        Performance performance = performanceMap.get(bookmark.getPerformanceId());

        return new BookmarkedPerformancePageResponse(
                performance.getId(),
                fileUrlMap.get(performance.getFileId()),
                performance.getTitle(),
                performance.getPrice(),
                performance.getStartDate(),
                performance.getEndDate(),
                performance.getVenue(),
                performance.getCategory(),
                performance.getStatus(),
                true // 북마크 목록이므로 모두 북마크되어 있음
        );
    }
}
