package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void signupTest() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("user@mail.com", "password", "USER");

        SignupResponse signupResponse = new SignupResponse("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwiZW1haWwiOiJ1c2VyNUBtYWlsLmNvbSIsInVzZXJSb2xlIjoiQURNSU4iLCJleHAiOjE3MjYwNDc1MDksImlhdCI6MTcyNjA0MzkwOX0.68QmqeMOfXLw5a_qjHPbr_4JfXgN6yXbuAk8I7YE4Ww");

        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void signinTest() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("user@mail.com", "password");

        SigninResponse signinResponse = new SigninResponse("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwiZW1haWwiOiJ1c2VyNUBtYWlsLmNvbSIsInVzZXJSb2xlIjoiQURNSU4iLCJleHAiOjE3MjYwNDc1MDksImlhdCI6MTcyNjA0MzkwOX0.68QmqeMOfXLw5a_qjHPbr_4JfXgN6yXbuAk8I7YE4Ww");

        given(authService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest)));

        // then
        resultActions.andExpect(status().isOk());
    }
}