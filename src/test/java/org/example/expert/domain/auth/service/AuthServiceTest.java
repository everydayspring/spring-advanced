package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class SignupTest {
        @Test
        void 회원가입_정상동작() {
            // given
            SignupRequest signupRequest = new SignupRequest("email", "pwd", "USER");

            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", UserRole.USER);

            long userId = 1L;

            ReflectionTestUtils.setField(user, "id", userId);

            String bearerToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZW1haWwiOiJ1c2VyNEBtYWlsLmNvbSIsInVzZXJSb2xlIjoiVVNFUiIsImV4cCI6MTcyNjA0NDU4NCwiaWF0IjoxNzI2MDQwOTg0fQ.jaMMtapDd9DZcTmztJv-zk2vpUGxge9UiFhxCCt7KfE";

            given(userRepository.existsByEmail(anyString())).willReturn(false);

            given(userRepository.save(any(User.class))).willReturn(user);

            given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn(bearerToken);

            // when
            SignupResponse signupResponse = authService.signup(signupRequest);

            // then
            assertNotNull(signupResponse.getBearerToken());

            assertEquals(bearerToken, signupResponse.getBearerToken());
        }

        @Test
        void 회원가입중_중복된_이메일로_에러발생() {
            // given
            SignupRequest signupRequest = new SignupRequest("email", "pwd", "USER");

            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

            // then
            assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        }

        @Test
        void 회원가입중_유효하지_않은_권한값으로_에러발생() {
            // given
            SignupRequest signupRequest = new SignupRequest("email", "pwd", "유효하지_않은_권한값");

            given(userRepository.existsByEmail(anyString())).willReturn(false);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

            // then
            assertEquals("유효하지 않은 UserRole", exception.getMessage());
        }
    }

    @Nested
    class SigninTest {
        @Test
        void 로그인_정상동작() {
            // given
            SigninRequest signinRequest = new SigninRequest("email", "pwd");

            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", UserRole.USER);

            long userId = 1L;

            ReflectionTestUtils.setField(user, "id", userId);

            String bearerToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZW1haWwiOiJ1c2VyNEBtYWlsLmNvbSIsInVzZXJSb2xlIjoiVVNFUiIsImV4cCI6MTcyNjA0NDU4NCwiaWF0IjoxNzI2MDQwOTg0fQ.jaMMtapDd9DZcTmztJv-zk2vpUGxge9UiFhxCCt7KfE";

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn(bearerToken);

            // when
            SigninResponse signinResponse = authService.signin(signinRequest);

            // then
            assertNotNull(signinResponse.getBearerToken());

            assertEquals(bearerToken, signinResponse.getBearerToken());
        }

        @Test
        void 로그인중_존재하지_않는_유저로_에러발생() {
            // given
            SigninRequest signinRequest = new SigninRequest("email", "pwd");

            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));

            // then
            assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
        }

        @Test
        void 로그인중_잘못된_비밀번호로_에러발생() {
            // given
            SigninRequest signinRequest = new SigninRequest("email", "pwd");

            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", UserRole.USER);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when
            AuthException exception = assertThrows(AuthException.class, () -> authService.signin(signinRequest));

            // then
            assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
        }
    }
}
