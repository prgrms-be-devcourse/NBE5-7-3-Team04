// package me.performancereservation.global.init;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import me.performancereservation.domain.bookmark.Bookmark;
// import me.performancereservation.domain.bookmark.BookmarkRepository;
// import me.performancereservation.domain.performance.entities.Performance;
// import me.performancereservation.domain.performance.entities.PerformanceSchedule;
// import me.performancereservation.domain.performance.enums.PerformanceCategory;
// import me.performancereservation.domain.performance.enums.PerformanceStatus;
// import me.performancereservation.domain.performance.repository.PerformanceRepository;
// import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
// import me.performancereservation.domain.refund.Refund;
// import me.performancereservation.domain.refund.RefundRepository;
// import me.performancereservation.domain.refund.enums.RefundStatus;
// import me.performancereservation.domain.reservation.Reservation;
// import me.performancereservation.domain.reservation.ReservationRepository;
// import me.performancereservation.domain.reservation.enums.ReservationStatus;
// import me.performancereservation.domain.review.Review;
// import me.performancereservation.domain.review.repository.ReviewRepository;
// import me.performancereservation.domain.settlement.Settlement;
// import me.performancereservation.domain.settlement.SettlementRepository;
// import me.performancereservation.domain.settlement.enums.SettlementStatus;
// import me.performancereservation.domain.ticket.Ticket;
// import me.performancereservation.domain.ticket.TicketRepository;
// import me.performancereservation.domain.ticket.enums.TicketStatus;
// import me.performancereservation.domain.user.entitiy.ManagerRequest;
// import me.performancereservation.domain.user.entitiy.User;
// import me.performancereservation.domain.user.enums.ManagerRequestStatus;
// import me.performancereservation.domain.user.enums.Role;
// import me.performancereservation.domain.user.repository.ManagerRequestRepository;
// import me.performancereservation.domain.user.repository.UserRepository;
// import me.performancereservation.global.storage.redis.RedisSeatService;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class MockDataLoader implements CommandLineRunner {
//     private final PerformanceRepository performanceRepository;
//     private final PerformanceScheduleRepository performanceScheduleRepository;
//     private final UserRepository userRepository;
//     private final ManagerRequestRepository managerRequestRepository;
//     private final ReservationRepository reservationRepository;
//     private final RefundRepository refundRepository;
//     private final SettlementRepository settlementRepository;
//     private final ReviewRepository reviewRepository;
//     private final BookmarkRepository bookmarkRepository;
//     private final TicketRepository ticketRepository;
//     private final RedisSeatService redisSeatService;
//     private final RedisTemplate<String, String> redisTemplate;

//     private final Random random = new Random();

//     @Override
//     public void run(String... args) {
// //        // Redis 초기화
//        redisTemplate.getConnectionFactory().getConnection().flushAll();

//         try {
//             // User 생성
//             List<User> users = createMockUsers();
//             userRepository.saveAll(users);

//             // ManagerRequest 생성
//             List<ManagerRequest> managerRequests = createManagerRequests(users);
//             managerRequestRepository.saveAll(managerRequests);

//             // Performance 생성
//             List<Performance> performances = createMockPerformances(users);
//             performanceRepository.saveAll(performances);

//             // PerformanceSchedule 생성
//             List<PerformanceSchedule> schedules = createMockSchedules(performances);
//             performanceScheduleRepository.saveAll(schedules);

//             // 좌석 Redis 초기화
//             for (PerformanceSchedule schedule : schedules) {
//                 redisSeatService.initializeSeatStock(schedule.getId(), schedule.getRemainingSeats());
//             }

//             // Reservation 생성
//             List<Reservation> reservations = createMockReservations(users, schedules);
//             reservationRepository.saveAll(reservations);


//             // 티켓 생성
//             List<Ticket> allTickets = new ArrayList<>();

//             for (Reservation reservation : reservations) {
//                 for (int j = 0; j < reservation.getQuantity(); j++) {
//                     allTickets.add(Ticket.builder()
//                             .reservationId(reservation.getId())
//                             .performanceId(reservation.getPerformanceId())
//                             .ticketStatus(TicketStatus.PENDING)
//                             .build());
//                 }
//             }

//             // 티켓 저장
//             ticketRepository.saveAll(allTickets);


//             // Reservation ID 값 확인
//             if (!reservations.isEmpty()) {
//                 System.out.println("Reservation 생성 완료. 첫 번째 ID: " + reservations.get(0).getId());
//             }

//             // Refund 생성
//             try {
//                 List<Refund> refunds = createMockRefunds(reservations);
//                 if (!refunds.isEmpty()) {
//                     refundRepository.saveAll(refunds);
//                     System.out.println("Refund 생성 완료.");
//                 } else {
//                     System.out.println("생성할 Refund가 없습니다.");
//                 }
//             } catch (Exception e) {
//                 System.err.println("Refund 생성 중 오류 발생: " + e.getMessage());
//                 e.printStackTrace();
//             }

//             // Settlement 생성
//             List<Settlement> settlements = createMockSettlements(performances);
//             settlementRepository.saveAll(settlements);

//             // Review 생성
//             List<Review> reviews = createMockReviews(users, performances, schedules);
//             reviewRepository.saveAll(reviews);

//             // Bookmark 생성
//             List<Bookmark> bookmarks = createMockBookmarks(users, performances);
//             bookmarkRepository.saveAll(bookmarks);

//             System.out.println("모든 테스트 데이터 생성이 완료되었습니다.");
//         } catch (Exception e) {
//             System.err.println("테스트 데이터 생성 중 오류 발생: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }

//     private List<User> createMockUsers() {
//         List<User> users = new ArrayList<>();

//         // ADMIN 유저 3명
//         for (int i = 1; i <= 3; i++) {
//             users.add(User.builder()
//                     .email("admin" + i + "@example.com")
//                     .name("관리자" + i)
//                     .phoneNumber("010-1111-000" + i)
//                     .role(Role.ADMIN)
//                     .build());
//         }

//         // MANAGER 유저 3명
//         for (int i = 1; i <= 3; i++) {
//             users.add(User.builder()
//                     .email("manager" + i + "@example.com")
//                     .name("매니저" + i)
//                     .phoneNumber("010-2222-000" + i)
//                     .role(Role.MANAGER)
//                     .build());
//         }

//         // 일반 USER 10명
//         for (int i = 1; i <= 10; i++) {
//             users.add(User.builder()
//                     .email("user" + i + "@example.com")
//                     .name("사용자" + i)
//                     .phoneNumber("010-3333-000" + i)
//                     .role(Role.USER)
//                     .build());
//         }

//         return users;
//     }

//     private List<ManagerRequest> createManagerRequests(List<User> users) {
//         List<ManagerRequest> requests = new ArrayList<>();

//         // PENDING 상태 요청 3개
//         for (int i = 0; i < 3; i++) {
//             User user = findUserByRole(users, Role.USER, i);
//             requests.add(ManagerRequest.builder()
//                     .userId(user.getId())
//                     .status(ManagerRequestStatus.PENDING)
//                     .build());
//         }

//         // APPROVED 상태 요청 3개
//         for (int i = 3; i < 6; i++) {
//             User user = findUserByRole(users, Role.USER, i);
//             requests.add(ManagerRequest.builder()
//                     .userId(user.getId())
//                     .status(ManagerRequestStatus.APPROVED)
//                     .approvedAt(LocalDate.now().minusDays(i))
//                     .build());
//         }

//         // REJECTED 상태 요청 3개
//         for (int i = 6; i < 9; i++) {
//             User user = findUserByRole(users, Role.USER, i);
//             requests.add(ManagerRequest.builder()
//                     .userId(user.getId())
//                     .status(ManagerRequestStatus.REJECTED)
//                     .build());
//         }

//         return requests;
//     }

//     private List<Performance> createMockPerformances(List<User> users) {
//         List<Performance> performances = new ArrayList<>();

//         // PENDING 상태 공연 3개
//         for (int i = 0; i < 3; i++) {
//             User manager = findUserByRole(users, Role.MANAGER, i % 3);
//             performances.add(Performance.builder()
//                     .category(getRandomCategory())
//                     .title("승인대기 공연 " + (i + 1))
//                     .description("승인 대기 중인 공연입니다. #" + (i + 1))
//                     .price(10000 + (i * 5000))
//                     .venue("서울 강남구 공연장 " + (i + 1))
//                     .managerId(manager.getId())
//                     .startDate(LocalDateTime.now().plusDays(10 + i))
//                     .endDate(LocalDateTime.now().plusDays(15 + i))
//                     .totalSeats(100 + (i * 50))
//                     .status(PerformanceStatus.PENDING)
//                     .build());
//         }

//         // CONFIRMED 상태 공연 3개
//         for (int i = 0; i < 3; i++) {
//             User manager = findUserByRole(users, Role.MANAGER, i % 3);
//             performances.add(Performance.builder()
//                     .category(getRandomCategory())
//                     .title("확정 공연 " + (i + 1))
//                     .description("공연 확정되었습니다. #" + (i + 1))
//                     .price(15000 + (i * 5000))
//                     .venue("서울 종로구 공연장 " + (i + 1))
//                     .managerId(manager.getId())
//                     .startDate(LocalDateTime.now().plusDays(5 + i))
//                     .endDate(LocalDateTime.now().plusDays(8 + i))
//                     .totalSeats(200 + (i * 50))
//                     .status(PerformanceStatus.CONFIRMED)
//                     .build());
//         }

//         // REJECTED 상태 공연 3개
//         for (int i = 0; i < 3; i++) {
//             User manager = findUserByRole(users, Role.MANAGER, i % 3);
//             performances.add(Performance.builder()
//                     .category(getRandomCategory())
//                     .title("거절된 공연 " + (i + 1))
//                     .description("공연 승인이 거절되었습니다. #" + (i + 1))
//                     .price(12000 + (i * 3000))
//                     .venue("서울 마포구 공연장 " + (i + 1))
//                     .managerId(manager.getId())
//                     .startDate(LocalDateTime.now().plusDays(15 + i))
//                     .endDate(LocalDateTime.now().plusDays(18 + i))
//                     .totalSeats(150 + (i * 30))
//                     .status(PerformanceStatus.REJECTED)
//                     .build());
//         }

//         // CANCELLED 상태 공연 3개
//         for (int i = 0; i < 3; i++) {
//             User manager = findUserByRole(users, Role.MANAGER, i % 3);
//             performances.add(Performance.builder()
//                     .category(getRandomCategory())
//                     .title("취소된 공연 " + (i + 1))
//                     .description("공연이 취소되었습니다. #" + (i + 1))
//                     .price(20000 + (i * 2000))
//                     .venue("부산 해운대구 공연장 " + (i + 1))
//                     .managerId(manager.getId())
//                     .startDate(LocalDateTime.now().plusDays(20 + i))
//                     .endDate(LocalDateTime.now().plusDays(22 + i))
//                     .totalSeats(120 + (i * 40))
//                     .status(PerformanceStatus.CANCELLED)
//                     .build());
//         }

//         // COMPLETED 상태 공연 3개
//         for (int i = 0; i < 3; i++) {
//             User manager = findUserByRole(users, Role.MANAGER, i % 3);
//             performances.add(Performance.builder()
//                     .category(getRandomCategory())
//                     .title("완료된 공연 " + (i + 1))
//                     .description("공연이 성공적으로 종료되었습니다. #" + (i + 1))
//                     .price(25000 + (i * 5000))
//                     .venue("인천 연수구 공연장 " + (i + 1))
//                     .managerId(manager.getId())
//                     .startDate(LocalDateTime.now().minusDays(10 + i))
//                     .endDate(LocalDateTime.now().minusDays(5 + i))
//                     .totalSeats(250 + (i * 50))
//                     .status(PerformanceStatus.COMPLETED)
//                     .build());
//         }

//         return performances;
//     }

//     private List<PerformanceSchedule> createMockSchedules(List<Performance> performances) {
//         List<PerformanceSchedule> allSchedules = new ArrayList<>();

//         // 각 공연별로 스케줄 생성 (CONFIRMED, COMPLETED 상태 공연만)
//         for (Performance performance : performances) {
//             if (performance.getStatus() == PerformanceStatus.CONFIRMED || performance.getStatus() == PerformanceStatus.COMPLETED) {
//                 LocalDateTime baseStartTime = performance.getStartDate();

//                 // 일반 스케줄 2개 생성
//                 for (int i = 0; i < 2; i++) {
//                     LocalDateTime startTime = baseStartTime.plusHours(i * 5);
//                     LocalDateTime endTime = startTime.plusHours(2);

//                     PerformanceSchedule schedule = PerformanceSchedule.builder()
//                             .performanceId(performance.getId())
//                             .startTime(startTime)
//                             .endTime(endTime)
//                             .remainingSeats(performance.getTotalSeats())
//                             .canceled(false)
//                             .build();

//                     allSchedules.add(schedule);
//                 }

//                 // 취소된 스케줄 1개 생성
//                 LocalDateTime startTimeCanceled = baseStartTime.plusHours(10);
//                 LocalDateTime endTimeCanceled = startTimeCanceled.plusHours(2);

//                 PerformanceSchedule canceledSchedule = PerformanceSchedule.builder()
//                         .performanceId(performance.getId())
//                         .startTime(startTimeCanceled)
//                         .endTime(endTimeCanceled)
//                         .remainingSeats(performance.getTotalSeats())
//                         .canceled(true)
//                         .build();

//                 allSchedules.add(canceledSchedule);
//             }
//         }

//         return allSchedules;
//     }

//     private List<Reservation> createMockReservations(List<User> users, List<PerformanceSchedule> schedules) {
//         List<Reservation> reservations = new ArrayList<>();
//         List<Ticket> allTickets = new ArrayList<>();

//         // 취소되지 않은 스케줄만 필터링
//         List<PerformanceSchedule> availableSchedules = schedules.stream()
//                 .filter(schedule -> !schedule.isCanceled())
//                 .toList();

//         if (availableSchedules.isEmpty()) {
//             return reservations;
//         }

//         // 일반 유저 필터링
//         List<User> normalUsers = users.stream()
//                 .filter(u -> u.getRole() == Role.USER)
//                 .toList();

//         if (normalUsers.isEmpty()) {
//             return reservations;
//         }

//         // PAYMENTS_PENDING 상태 예약 3개
//         for (int i = 0; i < 3 && i < normalUsers.size() && i < availableSchedules.size(); i++) {
//             User user = normalUsers.get(i);
//             PerformanceSchedule schedule = availableSchedules.get(i % availableSchedules.size());

//             reservations.add(Reservation.builder()
//                     .userId(user.getId())
//                     .performanceId(schedule.getPerformanceId())
//                     .scheduleId(schedule.getId())
//                     .quantity(1 + i)
//                     .status(ReservationStatus.PAYMENTS_PENDING)
//                     .build());
//         }

//         // PAYMENTS_CONFIRMED 상태 예약 3개
//         for (int i = 0; i < 3 && i + 3 < normalUsers.size() && i < availableSchedules.size(); i++) {
//             User user = normalUsers.get(i + 3);
//             PerformanceSchedule schedule = availableSchedules.get((i + 1) % availableSchedules.size());

//             reservations.add(Reservation.builder()
//                     .userId(user.getId())
//                     .performanceId(schedule.getPerformanceId())
//                     .scheduleId(schedule.getId())
//                     .quantity(2 + i)
//                     .status(ReservationStatus.PAYMENTS_CONFIRMED)
//                     .build());
//         }

//         // CANCEL_PENDING 상태 예약 3개
//         for (int i = 0; i < 3 && i + 6 < normalUsers.size() && i < availableSchedules.size(); i++) {
//             User user = normalUsers.get(i + 6);
//             PerformanceSchedule schedule = availableSchedules.get((i + 2) % availableSchedules.size());

//             reservations.add(Reservation.builder()
//                     .userId(user.getId())
//                     .performanceId(schedule.getPerformanceId())
//                     .scheduleId(schedule.getId())
//                     .quantity(1 + i)
//                     .status(ReservationStatus.CANCEL_PENDING)
//                     .build());
//         }

//         // CANCEL_CONFIRMED 상태 예약 6개
//         for (int i = 0; i < 6 && i < normalUsers.size() && i < availableSchedules.size(); i++) {
//             User user = normalUsers.get(i);
//             PerformanceSchedule schedule = availableSchedules.get(i % availableSchedules.size());

//             reservations.add(Reservation.builder()
//                     .userId(user.getId())
//                     .performanceId(schedule.getPerformanceId())
//                     .scheduleId(schedule.getId())
//                     .quantity(2 + i)
//                     .status(ReservationStatus.CANCEL_CONFIRMED)
//                     .build());
//         }

//         return reservations;
//     }

//     private List<Refund> createMockRefunds(List<Reservation> reservations) {
//         List<Refund> refunds = new ArrayList<>();

//         // 환불 가능한 예약 필터링 (취소 상태 예약)
//         List<Reservation> refundableReservations = reservations.stream()
//                 .filter(r -> r.getStatus() == ReservationStatus.CANCEL_PENDING || r.getStatus() == ReservationStatus.CANCEL_CONFIRMED)
//                 .filter(r -> r.getId() != null) // null ID 체크 추가
//                 .toList();

//         log.info("====================== refundableReservation 크기 : {}", refundableReservations.size());

//         if (refundableReservations.isEmpty()) {
//             return refunds;
//         }

//         // 각 상태별로 3개씩 환불 생성
//         for (int i = 0; i < refundableReservations.size(); i++) {
//             Reservation reservation = refundableReservations.get(i % refundableReservations.size());
//             RefundStatus status;

//             if (i < 3) {
//                 status = RefundStatus.PENDING;
//             } else if (i < 6) {
//                 status = RefundStatus.READY;
//             } else {
//                 status = RefundStatus.CONFIRMED;
//             }

//             Refund refund = Refund.builder()
//                     .id(null)
//                     .reservationId(reservation.getId())
//                     .userId(reservation.getUserId())
//                     .status(status)
//                     .build();

//             if (status == RefundStatus.READY || status == RefundStatus.CONFIRMED) {
//                 String bankName = status == RefundStatus.READY ? "신한은행" : "국민은행";
//                 String accountNumber = status == RefundStatus.READY ? "110-123-456789" : "110-456-789012";
//                 String accountHolder = status == RefundStatus.READY ? "환불자" + i : "환불완료자" + i;
//                 refund.updateBankInfo(accountNumber, bankName, accountHolder);
//             }

//             refunds.add(refund);
//         }

//         return refunds;
//     }

//     private List<Settlement> createMockSettlements(List<Performance> performances) {
//         List<Settlement> settlements = new ArrayList<>();

//         // 정산 가능한 공연 필터링 (COMPLETED 상태 공연만)
//         List<Performance> completedPerformances = performances.stream()
//                 .filter(p -> p.getStatus() == PerformanceStatus.COMPLETED)
//                 .toList();

//         // 완료된 공연이 없는 경우 CONFIRMED 상태 공연들도 포함
//         List<Performance> settleablePerformances = completedPerformances.isEmpty()
//                 ? performances.stream()
//                 .filter(p -> p.getStatus() == PerformanceStatus.CONFIRMED)
//                 .toList()
//                 : completedPerformances;

//         // 정산 가능한 공연이 없으면 빈 리스트 반환
//         if (settleablePerformances.isEmpty()) {
//             System.out.println("정산 가능한 공연이 없습니다.");
//             return settlements;
//         }

//         try {
//             // PENDING 상태 정산 3개
//             for (int i = 0; i < 3 && i < settleablePerformances.size(); i++) {
//                 Performance performance = settleablePerformances.get(i);

//                 settlements.add(Settlement.builder()
//                         .id(null) // ID는 자동 생성되도록 null 설정
//                         .performanceId(performance.getId())
//                         .totalAmount(performance.getPrice() * 50) // 가정: 50장 판매
//                         .account("123-456-789012")
//                         .bank("우리은행")
//                         .status(SettlementStatus.PENDING)
//                         .build());
//             }

//             // CONFIRMED 상태 정산 3개
//             for (int i = 0; i < 3 && i < settleablePerformances.size(); i++) {
//                 int idx = (i + 3) % settleablePerformances.size();
//                 Performance performance = settleablePerformances.get(idx);

//                 Settlement settlement = Settlement.builder()
//                         .id(null) // ID는 자동 생성되도록 null 설정
//                         .performanceId(performance.getId())
//                         .totalAmount(performance.getPrice() * 30) // 가정: 30장 판매
//                         .account("123-789-456012")
//                         .bank("하나은행")
//                         .status(SettlementStatus.CONFIRMED)
//                         .build();

//                 settlement.confirm(); // 정산 완료 처리
//                 settlements.add(settlement);
//             }
//         } catch (Exception e) {
//             System.err.println("Settlement 생성 중 오류 발생: " + e.getMessage());
//             e.printStackTrace();
//         }

//         return settlements;
//     }

//     private List<Review> createMockReviews(List<User> users, List<Performance> performances, List<PerformanceSchedule> schedules) {
//         List<Review> reviews = new ArrayList<>();

//         // 리뷰 작성 가능한 공연 필터링 (CONFIRMED 또는 COMPLETED 상태)
//         List<Performance> reviewablePerformances = performances.stream()
//                 .filter(p -> p.getStatus() == PerformanceStatus.CONFIRMED || p.getStatus() == PerformanceStatus.COMPLETED)
//                 .toList();

//         if (reviewablePerformances.isEmpty() || schedules.isEmpty() || users.isEmpty()) {
//             return reviews;
//         }

//         // 일반 유저들만 필터링
//         List<User> normalUsers = users.stream()
//                 .filter(u -> u.getRole() == Role.USER)
//                 .toList();

//         // 최소 9개의 리뷰 생성
//         for (int i = 0; i < 9 && i < normalUsers.size() && i < reviewablePerformances.size() && i < schedules.size(); i++) {
//             User user = normalUsers.get(i % normalUsers.size());
//             Performance performance = reviewablePerformances.get(i % reviewablePerformances.size());

//             // 해당 공연의 스케줄 찾기
//             List<PerformanceSchedule> performanceSchedules = schedules.stream()
//                     .filter(s -> s.getPerformanceId().equals(performance.getId()) && !s.isCanceled())
//                     .toList();

//             if (performanceSchedules.isEmpty()) continue;

//             PerformanceSchedule schedule = performanceSchedules.get(0);

//             String[] reviewComments = {
//                     "정말 멋진 공연이었습니다! 배우들의 연기가 훌륭했어요.",
//                     "음향과 조명이 아쉬웠지만 전반적으로 좋은 공연이었습니다.",
//                     "또 보고 싶을 정도로 감동적인 무대였습니다.",
//                     "기대보다는 조금 아쉬웠지만 나름 볼만했어요.",
//                     "티켓 값이 아깝지 않은 훌륭한 공연이었습니다!",
//                     "친구와 함께 봤는데 정말 만족스러웠습니다.",
//                     "연출과 무대 세트가 인상적이었습니다.",
//                     "배우들의 열정이 느껴지는 공연이었습니다.",
//                     "다음에도 이런 공연이 있다면 꼭 다시 보고 싶어요."
//             };

//             reviews.add(Review.builder()
//                     .userId(user.getId())
//                     .performanceId(performance.getId())
//                     .comments(reviewComments[i % reviewComments.length])
//                     .build());
//         }

//         return reviews;
//     }

//     private List<Bookmark> createMockBookmarks(List<User> users, List<Performance> performances) {
//         List<Bookmark> bookmarks = new ArrayList<>();

//         // 북마크 가능한 공연 필터링 (CONFIRMED 상태)
//         List<Performance> bookmarkablePerformances = performances.stream()
//                 .filter(p -> p.getStatus() == PerformanceStatus.CONFIRMED)
//                 .toList();

//         if (bookmarkablePerformances.isEmpty()) {
//             return bookmarks;
//         }

//         // 일반 유저들만 필터링
//         List<User> normalUsers = users.stream()
//                 .filter(u -> u.getRole() == Role.USER)
//                 .toList();

//         // 최소 9개의 북마크 생성
//         for (int i = 0; i < 9 && i < normalUsers.size(); i++) {
//             User user = normalUsers.get(i % normalUsers.size());

//             // 각 유저는 최대 3개의 공연을 북마크
//             for (int j = 0; j < 3 && j < bookmarkablePerformances.size(); j++) {
//                 Performance performance = bookmarkablePerformances.get((i + j) % bookmarkablePerformances.size());

//                 bookmarks.add(Bookmark.builder()
//                         .userId(user.getId())
//                         .performanceId(performance.getId())
//                         .build());
//             }
//         }

//         return bookmarks;
//     }

//     // 주어진 역할의 유저 찾기
//     private User findUserByRole(List<User> users, Role role, int index) {
//         List<User> filteredUsers = users.stream()
//                 .filter(u -> u.getRole() == role)
//                 .toList();

//         if (filteredUsers.isEmpty()) {
//             return users.get(0); // 해당 역할의 유저가 없으면 첫 번째 유저 반환
//         }

//         return filteredUsers.get(index % filteredUsers.size());
//     }

//     // 랜덤 카테고리 선택
//     private PerformanceCategory getRandomCategory() {
//         PerformanceCategory[] categories = PerformanceCategory.values();
//         return categories[random.nextInt(categories.length)];
//     }
// }
