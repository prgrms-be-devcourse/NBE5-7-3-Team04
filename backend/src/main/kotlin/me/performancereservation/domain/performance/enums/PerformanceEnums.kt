package me.performancereservation.domain.performance.enums

// 공연 카테고리
enum class PerformanceCategory {
    CLASSIC_DANCE,  // 클래식 + 무용
    EVENT_DISPLAY,  // 행사 + 전시
    CONCERT,  // 콘서트
    MUSICAL_OPERA,  // 뮤지컬 + 오페라
    THEATER,  // 연극
    ETC // 기타
}

// 공연 상태
enum class PerformanceStatus {
    PENDING,  //승인 요청 전
    CONFIRMED,  //승인됨 (등록)
    REJECTED,  //거부됨

    CANCELLED,  //취소됨
    COMPLETED //공연 완료됨
}
