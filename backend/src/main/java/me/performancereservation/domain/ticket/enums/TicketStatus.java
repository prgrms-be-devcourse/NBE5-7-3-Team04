package me.performancereservation.domain.ticket.enums;

public enum TicketStatus {
    PENDING, // 티켓 사용 전
    USED, // 티켓 사용 완료
    CANCELLED, // 취소
    EXPIRED // 만료
}
