package me.performancereservation.domain.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.RefundRequest;
import me.performancereservation.domain.refund.dto.RefundResponse;
import me.performancereservation.domain.refund.enums.RefundStatus;
import me.performancereservation.domain.refund.mapper.RefundDetailMapper;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final ReservationRepository reservationRepository;
    private final RefundDetailMapper refundDetailMapper;

    /** refundRequest를 받고 Refund를 생성하여 저장
     *
     * @param refundRequest
     * @return null if 예약상태 == PAYMENTS_PENDING. 예약상태만 CANCEL_CONFIRMED로 설정.
     * RETURNS refundId가 담긴 RefundResponse if 예약상태 == PAYMENTS_CONFIRMED
     */
    public RefundResponse save(RefundRequest refundRequest) {
        // 이미 같은 예약ID인 Refund가 존재한다면 예외 던짐 (이미 환불신청된 이력 있음)
        if (refundRepository.findRefundByReservationId(refundRequest.reservationId()).isPresent()) {
            throw ErrorCode.DUPLICATE_REFUND.domainException("이미 환불 신청된 예약입니다. reservationId: " + refundRequest.reservationId());
        }

        // 예약 상태 확인 위해 repo에서 예약 조회
        Reservation reservation = reservationRepository.findById(refundRequest.reservationId())
                .orElseThrow(); // 나중에 RESERVATION_NOT_FOUND 예약 에러 추가

        // PAYMENTS_PENDING 상태인 경우 바로 CONFIRMED로 설정. 환불생성 x
        if (reservation.getStatus() == ReservationStatus.PAYMENTS_PENDING) {

            // 예약 상태를 CANCEL_CONFIRMED로 변경
            reservation.setStatus(ReservationStatus.CANCEL_CONFIRMED);
            return null;
        }

        // 결제대기/결제승인 상태가 아닌 다른 상태였다면 잘못된 요청
        if (reservation.getStatus() != ReservationStatus.PAYMENTS_CONFIRMED) {
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("잘못된 예약 상태입니다. status: " + reservation.getStatus());
        }

        // PAYMENTS_CONFIRMED 상태
        Refund newRefund = Refund.builder()
                .reservationId(refundRequest.reservationId())
                .userId(refundRequest.userId())
                .account(refundRequest.account())
                .bank(refundRequest.bank())
                .status(RefundStatus.PENDING)
                .build();

        // 예약 상태를 CANCEL_PENDING로 변경
        reservation.setStatus(ReservationStatus.CANCEL_PENDING);

        Refund savedRefund = refundRepository.save(newRefund);

        // refund 저장 후 일부 내용을 dto에 담아 전달
        return RefundResponse.fromEntity(savedRefund);
    }

    /// 모든 id의 환불내역 디테일 조회
    @Transactional(readOnly = true)
    public Page<RefundDetailResponse> findAllRefundsDetail(Pageable pageable) {
        // 쿼리로 [Refund, 예약수량, 시작시간, 회차상태, Performance]의 리스트를 받아옴
        Page<Object[]> results = refundRepository.findAllRefundsWithDetails(pageable);
        return refundDetailMapper.toRefundDetailResponsePage(results);
    }

    /// 입력받은 id의 환불내역 디테일 조회
    @Transactional(readOnly = true)
    public Page<RefundDetailResponse> findAllRefundsDetailByUserId(Long userId, Pageable pageable) {
        Page<Object[]> results = refundRepository.findRefundsDetailByUserId(userId, pageable);
        return refundDetailMapper.toRefundDetailResponsePage(results);
    }

    /// refund id의 환불내역 디테일 조회. 없는 refundId인 경우 return null
    @Transactional(readOnly = true)
    public RefundDetailResponse findRefundsDetailByRefundId(Long refundId) {
        List<Object[]> results = refundRepository.findRefundsDetailByRefundId(refundId);

        // refundId로 조회한 결과가 없는 경우 get(0) 실행하기 전에 검사
        if (results.isEmpty()) {
            throw ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + refundId);
        }
        return refundDetailMapper.toRefundDetailResponse(results.get(0));
    }

    /// 특정 status의 환불내역 디테일 조회
    @Transactional(readOnly = true)
    public Page<RefundDetailResponse> findAllRefundsDetailByRefundStatus(RefundStatus refundStatus, Pageable pageable) {
        Page<Object[]> results = refundRepository.findRefundsDetailByStatus(refundStatus, pageable);
        return refundDetailMapper.toRefundDetailResponsePage(results);
    }


    /// CONFIRM 상태로 바꿀 때는 이 메서드 이용. 예약도 함께 상태변경해준다.
    @Transactional
    public void confirmRefund(Long id) {
        // id로 먼저 찾아보고 해당하는 Refund가 없다면 throw NO_SUCH_REFUND_ERROR
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + id));

        // refund domain에서 상태 업데이트
        refund.confirm();

        // 해당하는 예약을 찾아 CANCEL_CONFIRMED로 상태 변경
        Reservation reservation = reservationRepository.findById(refund.getReservationId()).orElseThrow(); // 나중에 ErrorCode 추가
        reservation.setStatus(ReservationStatus.CANCEL_CONFIRMED);
    }

    /// 전체 refund 목록 조회 (간단한 내용)
    @Transactional(readOnly = true)
    public List<RefundResponse> findAllRefunds() {
        List<Refund> foundRefunds = refundRepository.findAll();
        return foundRefunds.stream()
                .map(RefundResponse::fromEntity) //RefundResponse 내부의 fromEntity 메서드로 각각 변환
                .collect(Collectors.toList()); // stream-> list로 변환
    }

    /// 전체 refund 목록 상태별 조회 (간단한 내용)
    @Transactional(readOnly = true)
    public List<RefundResponse> findAllRefundByStatus(RefundStatus status) {
        List<Refund> foundRefunds = refundRepository.findRefundByStatus(status);
        return foundRefunds.stream()
                .map(RefundResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /// 원하는 환불 상태로 변경할 때 이용
    @Transactional
    public void updateRefundStatus(Long id, RefundStatus status) {
        refundRepository.findById(id)
                .orElseThrow(() -> ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + id));
        
        int updatedRows = refundRepository.updateRefundStatus(id, status);
        if (updatedRows == 0) {
            throw ErrorCode.REFUND_NOT_FOUND.domainException("환불 상태 업데이트 실패. refundId: " + id);
        }
    }




}
