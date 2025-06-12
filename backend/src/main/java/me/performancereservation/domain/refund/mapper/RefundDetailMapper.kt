package me.performancereservation.domain.refund.mapper;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.refund.Refund;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RefundDetailMapper {

    /**
     * 단일 Object[] 배열을 RefundDetailResponse로 변환
     * 배열의 순서: [Refund, reservationQuantity, startTime, Performance]
     */
    public RefundDetailResponse toRefundDetailResponse(Object[] result) {
        Refund refund = (Refund) result[0];
        Integer reservationQuantity = (Integer) result[1];
        LocalDateTime startTime = (LocalDateTime) result[2];
        Performance performance = (Performance) result[3];

        return RefundDetailResponse.fromEntity(refund, reservationQuantity, startTime, performance);
    }

    /**
     * Object[] 배열의 Page를 RefundDetailResponse의 Page로 변환
     * 
     * @param results Object[] 배열의 Page (각 배열은 [Refund, reservationQuantity, startTime, Performance] 순서)
     * @return RefundDetailResponse의 Page (페이지 정보 유지)
     */
    public Page<RefundDetailResponse> toRefundDetailResponsePage(Page<Object[]> results) {
        // 현재 페이지의 데이터를 RefundDetailResponse로 변환
        List<RefundDetailResponse> content = results.getContent().stream()
                .map(this::toRefundDetailResponse)
                .collect(Collectors.toList());
        
        // PageImpl 생성자를 통해 페이지 정보를 유지하면서 새로운 Page 객체 생성
        return new PageImpl<>(
            content,                    // 변환된 데이터 목록
            results.getPageable(),      // 페이지 정보
            results.getTotalElements()  // 전체 데이터 개수
        );
    }
} 