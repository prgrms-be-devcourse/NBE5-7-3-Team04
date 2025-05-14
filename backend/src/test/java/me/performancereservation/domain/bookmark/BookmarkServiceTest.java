package me.performancereservation.domain.bookmark;

import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse;
import me.performancereservation.domain.file.File;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("공연 북마크 성공")
    void performanceBookmark_Success() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        Performance performance = Performance.builder()
                .id(performanceId)
                .title("테스트 공연")
                .price(10000)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        Bookmark bookmark = Bookmark.builder()
                .id(1L)
                .userId(userId)
                .performanceId(performanceId)
                .build();

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.existsByUserIdAndPerformanceId(userId, performanceId)).thenReturn(false);
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(bookmark);

        // then
        assertDoesNotThrow(() -> bookmarkService.performanceBookmark(userId, performanceId));

        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository).existsByUserIdAndPerformanceId(userId, performanceId);
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("공연 북마크 실패 - 공연 없음")
    void performanceBookmark_Fail_PerformanceNotFound() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                bookmarkService.performanceBookmark(userId, performanceId)
        );

        assertEquals(ErrorCode.PERFORMANCE_NOT_FOUND, exception.getErrorCode());
        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository, never()).existsByUserIdAndPerformanceId(anyLong(), anyLong());
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("공연 북마크 실패 - 이미 북마크 존재")
    void performanceBookmark_Fail_DuplicatedBookmark() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        Performance performance = Performance.builder()
                .id(performanceId)
                .title("테스트 공연")
                .price(10000)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.existsByUserIdAndPerformanceId(userId, performanceId)).thenReturn(true);

        // then
        AppException exception = assertThrows(AppException.class, () ->
                bookmarkService.performanceBookmark(userId, performanceId)
        );

        assertEquals(ErrorCode.DUPLICATED_BOOKMARK, exception.getErrorCode());
        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository).existsByUserIdAndPerformanceId(userId, performanceId);
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("공연 북마크 취소 성공")
    void performanceBookmarkCancel_Success() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        Performance performance = Performance.builder()
                .id(performanceId)
                .title("테스트 공연")
                .price(10000)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        Bookmark bookmark = Bookmark.builder()
                .id(1L)
                .userId(userId)
                .performanceId(performanceId)
                .build();

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId)).thenReturn(Optional.of(bookmark));

        // then
        assertDoesNotThrow(() -> bookmarkService.performanceBookmarkCancel(userId, performanceId));

        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository).findByUserIdAndPerformanceId(userId, performanceId);
        verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    @DisplayName("공연 북마크 취소 실패 - 공연 없음")
    void performanceBookmarkCancel_Fail_PerformanceNotFound() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                bookmarkService.performanceBookmarkCancel(userId, performanceId)
        );

        assertEquals(ErrorCode.PERFORMANCE_NOT_FOUND, exception.getErrorCode());
        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository, never()).findByUserIdAndPerformanceId(anyLong(), anyLong());
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }

    @Test
    @DisplayName("공연 북마크 취소 실패 - 북마크 없음")
    void performanceBookmarkCancel_Fail_BookmarkNotFound() {
        // given
        Long userId = 1L;
        Long performanceId = 1L;

        Performance performance = Performance.builder()
                .id(performanceId)
                .title("테스트 공연")
                .price(10000)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        // when
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId)).thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                bookmarkService.performanceBookmarkCancel(userId, performanceId)
        );

        assertEquals(ErrorCode.BOOKMARK_NOT_FOUND, exception.getErrorCode());
        verify(performanceRepository).findById(performanceId);
        verify(bookmarkRepository).findByUserIdAndPerformanceId(userId, performanceId);
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크된 공연 목록 조회 성공")
    void getBookmarkedPerformances_Success() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // 북마크 목록
        List<Bookmark> bookmarks = List.of(
                Bookmark.builder().id(1L).userId(userId).performanceId(1L).build(),
                Bookmark.builder().id(2L).userId(userId).performanceId(2L).build()
        );

        // 공연 목록
        List<Performance> performances = List.of(
                Performance.builder()
                        .id(1L)
                        .fileId(1L)
                        .title("테스트 공연 1")
                        .price(10000)
                        .venue("테스트 장소 1")
                        .description("테스트 설명 1")
                        .category(PerformanceCategory.OPERA)
                        .status(PerformanceStatus.CONFIRMED)
                        .startDate(LocalDateTime.now().plusDays(1))
                        .endDate(LocalDateTime.now().plusDays(2))
                        .build(),
                Performance.builder()
                        .id(2L)
                        .fileId(2L)
                        .title("테스트 공연 2")
                        .price(20000)
                        .venue("테스트 장소 2")
                        .description("테스트 설명 2")
                        .category(PerformanceCategory.OPERA)
                        .status(PerformanceStatus.CONFIRMED)
                        .startDate(LocalDateTime.now().plusDays(2))
                        .endDate(LocalDateTime.now().plusDays(3))
                        .build()
        );

        // 파일 목록
        List<File> files = List.of(
                File.builder().id(1L).key("test-file-url-1").build(),
                File.builder().id(2L).key("test-file-url-2").build()
        );

        // when
        when(bookmarkRepository.findAllByUserId(userId, pageable)).thenReturn(new PageImpl<>(bookmarks, pageable, bookmarks.size()));
        when(performanceRepository.findAllById(anyList())).thenReturn(performances);
        when(fileRepository.findAllById(anyList())).thenReturn(files);

        Page<BookmarkedPerformancePageResponse> result = bookmarkService.getBookmarkedPerformances(userId, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());

        BookmarkedPerformancePageResponse response1 = result.getContent().get(0);
        assertEquals(1L, response1.id());
        assertEquals("test-file-url-1", response1.fileUrl());
        assertEquals("테스트 공연 1", response1.title());
        assertEquals(10000, response1.price());
        assertEquals("테스트 장소 1", response1.venue());
        assertEquals(PerformanceCategory.OPERA, response1.category());
        assertEquals(PerformanceStatus.CONFIRMED, response1.status());
        assertTrue(response1.bookmarked());

        BookmarkedPerformancePageResponse response2 = result.getContent().get(1);
        assertEquals(2L, response2.id());
        assertEquals("test-file-url-2", response2.fileUrl());
        assertEquals("테스트 공연 2", response2.title());
        assertEquals(20000, response2.price());
        assertEquals("테스트 장소 2", response2.venue());
        assertEquals(PerformanceCategory.OPERA, response2.category());
        assertEquals(PerformanceStatus.CONFIRMED, response2.status());
        assertTrue(response2.bookmarked());

        verify(bookmarkRepository).findAllByUserId(userId, pageable);
        verify(performanceRepository).findAllById(anyList());
        verify(fileRepository).findAllById(anyList());
    }
}