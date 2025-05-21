# 4조 공연 예매 서비스

팀원: [김광현](https://github.com/kwang2134), [탁서윤](https://github.com/peng255), [이상진](https://github.com/silkair), [황치연](https://github.com/inswal843), [김수혁](https://github.com/Soohyeok447)

## 프로젝트 소개
![logo-text](https://github.com/user-attachments/assets/ab6fbaf6-9c84-4ed2-8cd3-650705d23e42)
![logo-icon](https://github.com/user-attachments/assets/2befc3d6-65b6-4179-b8ec-9341d53e8b04)

관람객과 공연 기획자 사이에서 공연 예매를 중개해주는 사이트.

누구나 공연 기획자 신청을 하고 자신의 공연 만들기 가능

유튜브 사용자는 유튜브를 이용하면서 동시에 콘텐츠 공급자가 될 수 있는 것처럼, <br>
티켓포유 사용자들도 자신만의 공연을 등록할 수 있다. 

단, 공연관리자 신청 후 승인을 받는 과정이 있음

예매를 했을 때 바로 공연 기획 측에 수익이 들어가는 것이 아니라 플랫폼 측에서 보관하다가 공연이 완료 일주일 후에 전달하는 것이 특징 (ex. 당근페이 번개페이)

## 타겟 사용자

일반 관람객, 공연 기획자, 플랫폼 관리자(티켓포유)

## 사용 기술

| 항목          | 기술                                                  |
| ------------- | ----------------------------------------------------- |
| Framework     | SpringBoot                                            |
| JDK           | 21                                                    |
| DB            | MySQL, Redis, JPA, Spring Data JPA, Spring Data Redis |
| Security      | OAuth2.0, JWT, formLogin                              |
| DevOps        | AWS EC2, Docker, Docker-Compose, Github actions       |
| Cloud Service | AWS S3, AWS Cloudfront, CoolSMS                       |

## 팀컨벤션

[컨벤션](docs/컨벤션.md)

## 핵심 기능

**[사용자]**

공연 목록 조회 / 필터링, 공연 상세 조회, 공연 예매, 예매 취소, 환불

**[기획자]**

공연/회차 등록&수정&취소, 정산 요청

**[관리자]**

공연, 기획자 승인/거부, 유저 무통장입금 환불, 기획자 정산

**[로그인 방식]**

일반 사용자 : OAuth2.0 구글, 네이버, 카카오 + JWT token

ADMIN : /admin/login으로 접속 후 폼 로그인

## ERD
![티켓포유](https://github.com/user-attachments/assets/0281a112-c655-4df7-aa3a-8a0c1252f50d)

## 시스템 아키텍쳐
![티켓포유](https://github.com/user-attachments/assets/c01639b4-e0b7-4289-b384-f11cb8878438)

## 플로우 차트
![티켓포유 플로우](https://github.com/user-attachments/assets/6048b061-0b8c-4d68-9646-1ab5c2b7c4bd)


## 고려한 점

-   ‘공연과 예매’라는 비즈니스 플로우 내에서 연관된 도메인 엔티티가 많아서 상태 전이에 대한 관리가 중요했음
    → 순서도를 그려서 모든 팀원이 같은 비즈니스 로직을 이해할 수 있게 해결
-   환불 서비스를 고려하면서 공연이 취소될 수 있는 여러 상황 가정이 중요했다.
    → 각 분기를 분리하여 상황을 정리하고 알맞은 서비스 응답을 구현
-   공연 예매 사이트 특성상 초기 서비스라 할 지라도 갑자기 트래픽이 몰리는 경우를 고려하지 않을 수 없었는데 성능을 고려해서

## 추후 발전 방향

-   정산 시 수수료를 받아 수익 창출
-   좌석 기능 추가 + 선점 기능
-   공연 랭킹 기능 추가
-   검색 최적화
-   접속 대기열 기능 추가
-   모니터링 + 로깅 추가
-   기간에 따른 환불 비율
-   웹앱 패키징

## 고려한 점

-   [레디스를 사용한 이유](docs/고려한점/레디스_사용한_이유.md)

## 트러블슈팅

-   [공연 취소 시, 예약 일괄 취소 로직 개선기](docs/트러블슈팅/공연취소비동기.md) <br>
-   [배포 시, EC2 - SSH 인증 오류 해결](docs/트러블슈팅/배포시EC2_SSH인증오류.md) <br>
-   [쿼리 조회 후, 반환값 처리 에러 해결](docs/트러블슈팅/쿼리_조회_후_반환값_처리_에러.md) <br>
-   [레디스 해킹 시도 분석](docs/트러블슈팅/레디스해킹.md) <br>
-   [타임존 문제 해결](docs/트러블슈팅/타임존.md) <br>
-   [어드민 계정 오류 해결](docs/트러블슈팅/어드민계정오류해결.md) <br>
