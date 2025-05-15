package me.performancereservation.domain.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.RefundResponse;
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest;
import me.performancereservation.domain.refund.enums.RefundStatus;
import me.performancereservation.domain.refund.mapper.RefundDetailMapper;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final ReservationRepository reservationRepository;
    private final RefundDetailMapper refundDetailMapper;

    private static final int BATCH_SIZE = 1000;

    /** 사용자가 단일 예약에 대해 환불 생성할 때 이용.
     * 예약id를 받고 Refund를 생성하여 저장
     *
     * @param reservation 환불을 생성할 예약. 예약서비스에서 인자를 넣어 호출한다
     */
    public void save(Reservation reservation) {

        Long reservationId = reservation.getId();
        Long userId = reservation.getUserId();

        // 이미 같은 예약ID인 Refund가 존재한다면 예외 던짐 (이미 환불신청된 이력 있음)
        if (refundRepository.findRefundByReservationId(reservationId).isPresent()) {
            throw ErrorCode.DUPLICATE_REFUND.domainException("이미 환불 신청된 예약입니다. reservationId: " + reservationId);
        }

        // PENDING 상태로 환불 생성 후 저장
        Refund newRefund = Refund.builder()
                .reservationId(reservationId)
                .userId(userId)
                .status(RefundStatus.PENDING) // 계좌정보 입력 기다림
                .build();

        refundRepository.save(newRefund);

    }

    /// 모든 id의 환불 디테일 페이지 조회
    @Transactional(readOnly = true)
    public Page<RefundDetailResponse> findAllRefundsDetail(String status, Pageable pageable) {

        if (status == null){
            // 쿼리로 [Refund, 예약수량, 시작시간, 회차상태, Performance]의 리스트를 받아옴
            Page<Object[]> results = refundRepository.findAllRefundsWithDetails(pageable);
            return refundDetailMapper.toRefundDetailResponsePage(results);
        }

        // string -> RefundStatus로 변환
        RefundStatus refundStatus = getRefundStatus(status);

        Page<Object[]> results = refundRepository.findRefundsDetailByStatus(refundStatus, pageable);
        return refundDetailMapper.toRefundDetailResponsePage(results);
    }

    /// 입력받은 id의 환불 디테일 페이지 조회
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

    // string -> RefundStatus로 변환. 변환 불가능할 경우 throw exception
    private static RefundStatus getRefundStatus(String refundStatus) {
        RefundStatus status;

        try { // 문자열 쿼리 파라미터를 대문자로 변환하여 RefundStatus 생성 시도
            status = RefundStatus.valueOf(refundStatus.toUpperCase());

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 종류의 refundStatus 문자열이 들어왔을 경우
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("유효하지 않은 종류의 refund status로 생성요청. status: "+ refundStatus);
        }
        return status;
    }


    /// 특정 환불 상태로 변경할 때 이용
    @Transactional
    public void updateRefundStatus(Long id, String status) {
        Refund foundRefund = refundRepository.findById(id)
                .orElseThrow(() -> ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + id));

        foundRefund.setStatus(getRefundStatus(status));
    }

    /// refund를 CONFIRM 상태로 바꾼다. 예약서비스에서 호출 예정
    @Transactional
    public void confirmRefund(Long id) {
        // id로 먼저 찾아보고 해당하는 Refund가 없다면 throw NO_SUCH_REFUND_ERROR
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + id));

        // refund domain에서 상태 업데이트
        refund.confirm();
    }

    ///  계좌, 은행, 입금자명 설정, READY state 설정
    @Transactional
    public Refund updateBankInfo(Long userId, UpdateBankInfoRequest request) {
        // 해당 refund 존재하는지 유효성검사
        Refund refund = refundRepository.findById(request.refundId())
                .orElseThrow(() -> ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: " + request.refundId()));

        // 정보를 변경하려는 환불의 user id가 현재 로그인된 user id와 다를 경우 거부
        if (refund.getUserId() != userId) {
            ErrorCode.UNAUTHORIZED_REFUND_UPDATE.domainException("본인의 환불만 변경할 수 있습니다.");
        }

        // 계좌정보 설정
        refund.updateBankInfo(request.account(), request.bank(), request.depositorName());
        // PENDING -> READY 설정
        refund.ready();

        return refund;
    }

    /// 전체 refund 목록 조회 (간단한 내용)
    @Transactional(readOnly = true)
    public List<RefundResponse> findAllRefunds() {
        List<Refund> foundRefunds = refundRepository.findAll();
        return foundRefunds.stream()
                .map(RefundResponse::fromEntity) //RefundResponse 내부의 fromEntity 메서드로 각각 변환
                .toList(); // stream-> list로 변환
    }

    /// 전체 refund 목록 상태별 조회 (간단한 내용)
    @Transactional(readOnly = true)
    public List<RefundResponse> findAllRefundByStatus(RefundStatus status) {
        List<Refund> foundRefunds = refundRepository.findRefundByStatus(status);
        return foundRefunds.stream()
                .map(RefundResponse::fromEntity)
                .toList();
    }

    /**
     * 대량의 예약에 대한 환불을 일괄적으로 생성합니다.
     * 결제승인 상태의 예약들에 대해 환불을 생성하며, 벌크 인서트와 배치 처리를 적용합니다.
     *
     * @param reservationList 환불을 생성할 예약 목록
     */
    @Transactional
    public void saveRefundFromReservationList(List<Reservation> reservationList) {
        if (reservationList == null || reservationList.isEmpty()) {
            log.warn("환불 생성할 예약 목록이 비어있습니다.");
            return;
        }

        log.info("대량 환불 생성 시작: 예약 수={}", reservationList.size());

        // 이미 환불이 생성된 예약 ID 목록 조회
        List<Long> existingRefundReservationIds = refundRepository.findRefundByReservationIdIn(
                reservationList.stream()
                        .map(Reservation::getId)
                        .collect(Collectors.toList())
        );

        // 환불 생성할 예약 필터링 (이미 환불이 생성된 예약 제외)
        List<Refund> refundsToSave = reservationList.stream()
                .filter(reservation -> !existingRefundReservationIds.contains(reservation.getId()))
                .map(reservation -> Refund.builder()
                        .reservationId(reservation.getId())
                        .userId(reservation.getUserId())
                        .status(RefundStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        if (refundsToSave.isEmpty()) {
            log.info("생성할 환불이 없습니다.");
            return;
        }

        // 배치 단위로 나누어 저장
        List<List<Refund>> batches = new ArrayList<>();
        for (int i = 0; i < refundsToSave.size(); i += BATCH_SIZE) {
            batches.add(refundsToSave.subList(i, Math.min(i + BATCH_SIZE, refundsToSave.size())));
        }

        // 배치 단위로 저장 실행
        for (List<Refund> batch : batches) {
            refundRepository.saveAll(batch);
            log.info("환불 배치 저장 완료: {}개", batch.size());
        }

        log.info("대량 환불 생성 완료: 총 {}개 중 {}개 생성됨", 
                reservationList.size(), refundsToSave.size());
    }

}
