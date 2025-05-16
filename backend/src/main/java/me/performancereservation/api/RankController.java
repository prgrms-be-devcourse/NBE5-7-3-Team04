//package me.performancereservation.api;
//
//import lombok.RequiredArgsConstructor;
//import me.performancereservation.domain.rank.RankService;
//import me.performancereservation.domain.rank.dto.PerformanceRankingResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController("/api/v1")
//@RequiredArgsConstructor
//public class RankController {
//    private final RankService rankService;
//
//    /**
//     * 최근 3일간 인기 공연 순위를 10개 조회
//     *
//     * @return 최근 3일간 인기 공연 순위
//     */
//    @GetMapping("/rankings")
//    public ResponseEntity<List<PerformanceRankingResponse>> getTopPerformances() {
//
//        return ResponseEntity.ok(rankService.getRanking());
//    }
//}
