package me.performancereservation.domain.user.service;

import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.domain.user.dto.request.UserOnboardingRequest;
import me.performancereservation.domain.user.dto.request.UserManagerRequestRequest;
import me.performancereservation.domain.user.entity.ManagerRequest;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import me.performancereservation.domain.user.repository.ManagerRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerRequestRepository managerRequestRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String EMAIL = "test@example.com";
    private static final String NAME = "테스트유저";
    private static final String PHONE_NUMBER = "010-1234-5678";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String NEW_PHONE_NUMBER = "010-8765-4321";
    private static final String REASON = "공연 관리 경험이 있습니다.";
    private static final String EXPERIENCE = "3년간 공연장에서 일했습니다.";
    private static final String ORGANIZATION_NAME = "테스트 공연장";
    private static final String ORGANIZATION_CONTACT = "02-1234-5678";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email(EMAIL)
                .name(NAME)
                .phoneNumber(PHONE_NUMBER)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입: 성공")
    void registerUser_success() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User result = userService.registerUser(EMAIL, NAME, PHONE_NUMBER, Role.USER);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(result.getRole()).isEqualTo(Role.USER);

        // verify
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입: 이메일 중복")
    void registerUser_duplicateEmail() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(EMAIL, NAME, PHONE_NUMBER, Role.USER))
                .isInstanceOf(ErrorCode.DUPLICATE_USER_EMAIL.serviceException().getClass());

        // verify
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("ID로 유저 조회: 성공")
    void getUserById_success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when
        User result = userService.getUserById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(result.getRole()).isEqualTo(Role.USER);

        // verify
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("ID로 유저 조회: 실패")
    void getUserById_notFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ErrorCode.USER_NOT_FOUND.serviceException().getClass());

        // verify
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("이메일로 유저 조회: 성공")
    void getUserByEmail_success() {
        // given
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(testUser));

        // when
        User result = userService.getUserByEmail(EMAIL);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(result.getRole()).isEqualTo(Role.USER);

        // verify
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("이메일로 유저 조회: 실패")
    void getUserByEmail_notFound() {
        // given
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserByEmail(EMAIL))
                .isInstanceOf(ErrorCode.USER_NOT_FOUND.serviceException().getClass());

        // verify
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("추가 정보 입력: 전화번호만 업데이트")
    void onboard_phoneNumberOnly() {
        // given
        User user = User.builder()
                .id(1L)
                .email(EMAIL)
                .name(NAME)
                .role(Role.USER)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserOnboardingRequest request = new UserOnboardingRequest(NEW_PHONE_NUMBER, null);

        // when
        userService.onboard(1L, request);

        // then
        assertThat(user.getPhoneNumber()).isEqualTo(NEW_PHONE_NUMBER);
        assertThat(user.getEmail()).isEqualTo(EMAIL);

        // verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("추가 정보 입력: 이메일만 업데이트")
    void onboard_emailOnly() {
        // given
        User user = User.builder()
                .id(1L)
                .name(NAME)
                .phoneNumber(PHONE_NUMBER)
                .role(Role.USER)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserOnboardingRequest request = new UserOnboardingRequest(null, NEW_EMAIL);

        // when
        userService.onboard(1L, request);

        // then
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPhoneNumber()).isEqualTo(PHONE_NUMBER);

        // verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("추가 정보 입력: 모든 정보 업데이트")
    void onboard_allFields() {
        // given
        User user = User.builder()
                .id(1L)
                .name(NAME)
                .role(Role.USER)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserOnboardingRequest request = new UserOnboardingRequest(NEW_PHONE_NUMBER, NEW_EMAIL);

        // when
        userService.onboard(1L, request);

        // then
        assertThat(user.getPhoneNumber()).isEqualTo(NEW_PHONE_NUMBER);
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);

        // verify
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("추가 정보 입력: 유저가 존재하지 않는 경우")
    void onboard_userNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        UserOnboardingRequest request = new UserOnboardingRequest(NEW_PHONE_NUMBER, NEW_EMAIL);

        // when & then
        assertThatThrownBy(() -> userService.onboard(1L, request))
                .isInstanceOf(ErrorCode.USER_NOT_FOUND.serviceException().getClass());

        // verify
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("공연 관리자 권한 신청: 성공")
    void submitManagerRequest_success() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(false);
        given(managerRequestRepository.hasPendingRequest(1L)).willReturn(false);
        given(managerRequestRepository.save(any(ManagerRequest.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserManagerRequestRequest request = new UserManagerRequestRequest(
                ORGANIZATION_NAME,
                ORGANIZATION_CONTACT,
                EXPERIENCE,
                REASON
        );

        // when
        userService.submitManagerRequest(1L, request);

        // then
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository).hasPendingRequest(1L);
        verify(managerRequestRepository).save(argThat(managerRequest -> {
            assertThat(managerRequest.getUserId()).isEqualTo(1L);
            assertThat(managerRequest.getReason()).isEqualTo(REASON);
            assertThat(managerRequest.getExperience()).isEqualTo(EXPERIENCE);
            assertThat(managerRequest.getOrganizationName()).isEqualTo(ORGANIZATION_NAME);
            assertThat(managerRequest.getOrganizationContact()).isEqualTo(ORGANIZATION_CONTACT);
            assertThat(managerRequest.getStatus()).isEqualTo(ManagerRequestStatus.PENDING);
            return true;
        }));
    }

    @Test
    @DisplayName("공연 관리자 권한 신청: 이미 승인된 요청이 있는 경우")
    void submitManagerRequest_alreadyApproved() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(true);

        UserManagerRequestRequest request = new UserManagerRequestRequest(
                ORGANIZATION_NAME,
                ORGANIZATION_CONTACT,
                EXPERIENCE,
                REASON
        );

        // when & then
        assertThatThrownBy(() -> userService.submitManagerRequest(1L, request))
                .isInstanceOf(ErrorCode.MANAGER_REQUEST_ALREADY_EXISTS.domainException("이미 공연자 권한 요청 중이거나 공연 관리자 입니다").getClass());

        // verify
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository, never()).hasPendingRequest(anyLong());
        verify(managerRequestRepository, never()).save(any(ManagerRequest.class));
    }

    @Test
    @DisplayName("공연 관리자 권한 신청: 대기 중인 요청이 있는 경우")
    void submitManagerRequest_pendingRequest() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(false);
        given(managerRequestRepository.hasPendingRequest(1L)).willReturn(true);

        UserManagerRequestRequest request = new UserManagerRequestRequest(
                ORGANIZATION_NAME,
                ORGANIZATION_CONTACT,
                EXPERIENCE,
                REASON
        );

        // when & then
        assertThatThrownBy(() -> userService.submitManagerRequest(1L, request))
                .isInstanceOf(ErrorCode.MANAGER_REQUEST_ALREADY_EXISTS.domainException("이미 공연자 권한 요청 중이거나 공연 관리자 입니다").getClass());

        // verify
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository).hasPendingRequest(1L);
        verify(managerRequestRepository, never()).save(any(ManagerRequest.class));
    }

    @Test
    @DisplayName("매니저 권한 신청 가능 여부 확인: 가능한 경우")
    void canRequestManagerRole_success() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(false);
        given(managerRequestRepository.hasPendingRequest(1L)).willReturn(false);

        // when
        boolean result = userService.canRequestManagerRole(1L);

        // then
        assertThat(result).isTrue();

        // verify
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository).hasPendingRequest(1L);
    }

    @Test
    @DisplayName("매니저 권한 신청 가능 여부 확인: 이미 승인된 요청이 있는 경우")
    void canRequestManagerRole_alreadyApproved() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(true);

        // when
        boolean result = userService.canRequestManagerRole(1L);

        // then
        assertThat(result).isFalse();

        // verify
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository, never()).hasPendingRequest(anyLong());
    }

    @Test
    @DisplayName("매니저 권한 신청 가능 여부 확인: 대기 중인 요청이 있는 경우")
    void canRequestManagerRole_pendingRequest() {
        // given
        given(managerRequestRepository.hasApprovedRequest(1L)).willReturn(false);
        given(managerRequestRepository.hasPendingRequest(1L)).willReturn(true);

        // when
        boolean result = userService.canRequestManagerRole(1L);

        // then
        assertThat(result).isFalse();

        // verify
        verify(managerRequestRepository).hasApprovedRequest(1L);
        verify(managerRequestRepository).hasPendingRequest(1L);
    }
} 