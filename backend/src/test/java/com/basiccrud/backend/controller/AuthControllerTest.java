package com.basiccrud.backend.controller;

import com.basiccrud.backend.dto.AuthResponseDTO;
import com.basiccrud.backend.dto.LoginRequestDTO;
import com.basiccrud.backend.dto.RegisterRequestDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController - Testes de Integração")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponseDTO authResponse;
    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponseDTO.of("jwt_token_aqui", "maria@email.com", "Maria Souza");

        registerDTO = RegisterRequestDTO.builder()
                .name("Maria Souza")
                .email("maria@email.com")
                .password("senha123")
                .build();

        loginDTO = LoginRequestDTO.builder()
                .email("maria@email.com")
                .password("senha123")
                .build();
    }

    @Test
    @DisplayName("POST /auth/register - deve registrar usuário e retornar 201 com token")
    void register_deveRetornar201_quandoDadosValidos() throws Exception {
        given(authService.register(any())).willReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token_aqui"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    @DisplayName("POST /auth/register - deve retornar 400 quando dados inválidos")
    void register_deveRetornar400_quandoDadosInvalidos() throws Exception {
        RegisterRequestDTO invalid = RegisterRequestDTO.builder()
                .name("")
                .email("email-invalido")
                .password("123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @DisplayName("POST /auth/register - deve retornar 409 quando email já cadastrado")
    void register_deveRetornar409_quandoEmailJaExiste() throws Exception {
        given(authService.register(any()))
                .willThrow(new EmailAlreadyExistsException("maria@email.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/login - deve autenticar e retornar 200 com token")
    void login_deveRetornar200_quandoCredenciaisValidas() throws Exception {
        given(authService.login(any())).willReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_aqui"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - deve retornar 400 quando dados inválidos")
    void login_deveRetornar400_quandoDadosInvalidos() throws Exception {
        LoginRequestDTO invalid = LoginRequestDTO.builder()
                .email("nao-e-email")
                .password("")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @DisplayName("POST /auth/login - deve retornar 500 quando credenciais inválidas")
    void login_deveRetornar500_quandoCredenciaisInvalidas() throws Exception {
        given(authService.login(any()))
                .willThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isInternalServerError());
    }
}

