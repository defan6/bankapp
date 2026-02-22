package com.bankapp.userservice.controller;

import com.bankapp.common.client.userserviceauth.model.*;
import com.bankapp.userservice.exception.ExistsValidatorHandler;
import com.bankapp.userservice.exception.RegisterValidatorHandler;
import com.bankapp.userservice.exception.TokenNotFoundException;
import com.bankapp.userservice.exception.UserIdNotFoundException;
import com.bankapp.userservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthControllerV1.class)
@DisplayName("AuthControllerV1 Tests")
class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("POST /api/v1/auth/register - успешная регистрация")
        @WithMockUser
        void registerUser_Success() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest()
                    .email("test@example.com")
                    .password("SecurePass123");

            RegisterResponse response = new RegisterResponse()
                    .userId(UUID.randomUUID())
                    .email("test@example.com");

            given(authService.register(any(RegisterRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - регистрация с пустым email (400)")
        @WithMockUser
        void registerUser_EmptyEmail_BadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest()
                    .email("")
                    .password("SecurePass123");

            given(authService.register(any(RegisterRequest.class)))
                    .willThrow(new RegisterValidatorHandler(List.of("Email is required")));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - регистрация с невалидным email (400)")
        @WithMockUser
        void registerUser_InvalidEmail_BadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest()
                    .email("not-an-email")
                    .password("SecurePass123");

            given(authService.register(any(RegisterRequest.class)))
                    .willThrow(new RegisterValidatorHandler(List.of("Invalid email format")));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - регистрация с пустым паролем (400)")
        @WithMockUser
        void registerUser_EmptyPassword_BadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest()
                    .email("test@example.com")
                    .password("");

            given(authService.register(any(RegisterRequest.class)))
                    .willThrow(new RegisterValidatorHandler(List.of("Password is required")));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - регистрация с уже существующим email (409)")
        @WithMockUser
        void registerUser_DuplicateEmail_Conflict() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest()
                    .email("existing@example.com")
                    .password("SecurePass123");

            given(authService.register(any(RegisterRequest.class)))
                    .willThrow(new ExistsValidatorHandler("User already exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("POST /api/v1/auth/login - успешный вход")
        @WithMockUser
        void loginUser_Success() throws Exception {
            // Given
            LoginRequest request = new LoginRequest()
                    .email("test@example.com")
                    .password("SecurePass123");

            LoginResponse response = new LoginResponse()
                    .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                    .refreshToken("refresh-token-12345");

            given(authService.login(any(LoginRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-12345"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - вход с неверным паролем (401)")
        @WithMockUser
        void loginUser_WrongPassword_Unauthorized() throws Exception {
            // Given
            LoginRequest request = new LoginRequest()
                    .email("test@example.com")
                    .password("WrongPassword");
            given(authService.login(any(LoginRequest.class))).willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - вход с несуществующим email (401/409)")
        @WithMockUser
        void loginUser_NonExistentEmail() throws Exception {
            // Given
            LoginRequest request = new LoginRequest()
                    .email("nonexistent@example.com")
                    .password("SecurePass123");
            given(authService.login(any(LoginRequest.class))).willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Refresh Tests")
    class RefreshTests {

        @Test
        @DisplayName("POST /api/v1/auth/refresh - успешное обновление токена")
        @WithMockUser
        void refreshAccessToken_Success() throws Exception {
            // Given
            RefreshTokenRequest request = new RefreshTokenRequest()
                    .refreshToken("valid-refresh-token");

            RefreshAccessTokenResponse response = new RefreshAccessTokenResponse()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token");

            given(authService.refresh(any(RefreshTokenRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

            verify(authService).refresh(any(RefreshTokenRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/auth/refresh - невалидный refresh токен (401)")
        @WithMockUser
        void refreshAccessToken_InvalidToken_Unauthorized() throws Exception {
            // Given
            RefreshTokenRequest request = new RefreshTokenRequest()
                    .refreshToken("invalid-refresh-token");

            given(authService.refresh(any(RefreshTokenRequest.class))).willThrow(new TokenNotFoundException("Token not exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("POST /api/v1/auth/logout - успешный выход (204)")
        @WithMockUser
        void logoutUser_Success() throws Exception {
            // Given
            LogoutRequest request = new LogoutRequest()
                    .accessToken("valid-access-token");

            LogoutResponse response = new LogoutResponse()
                    .message("Logout success");

            given(authService.logout(any(LogoutRequest.class))).willReturn(response);

            // When & Then - logout возвращает 204 No Content
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(authService).logout(any(LogoutRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/auth/logout - выход с невалидным токеном (400)")
        @WithMockUser
        void logoutUser_InvalidToken_BadRequest() throws Exception {
            // Given
            LogoutRequest request = new LogoutRequest()
                    .accessToken("invalid-access-token");

            given(authService.logout(any(LogoutRequest.class))).willThrow(new UserIdNotFoundException("User with id not be found"));
            // When & Then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
