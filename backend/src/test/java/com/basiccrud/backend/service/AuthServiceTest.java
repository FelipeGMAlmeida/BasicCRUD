package com.basiccrud.backend.service;

import com.basiccrud.backend.dto.AuthResponseDTO;
import com.basiccrud.backend.dto.LoginRequestDTO;
import com.basiccrud.backend.dto.RegisterRequestDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.model.User;
import com.basiccrud.backend.repository.UserRepository;
import com.basiccrud.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Souza")
                .email("maria@email.com")
                .password("encoded_password")
                .build();

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
    @DisplayName("register - deve registrar usuário e retornar token")
    void register_deveRetornarToken_quandoDadosValidos() {
        given(userRepository.existsByEmail(registerDTO.getEmail())).willReturn(false);
        given(passwordEncoder.encode(registerDTO.getPassword())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtService.generateToken(any(User.class))).willReturn("jwt_token");

        AuthResponseDTO response = authService.register(registerDTO);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("maria@email.com");
        assertThat(response.getName()).isEqualTo("Maria Souza");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - deve lançar exceção quando email já cadastrado")
    void register_deveLancarExcecao_quandoEmailJaCadastrado() {
        given(userRepository.existsByEmail(registerDTO.getEmail())).willReturn(true);

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - deve autenticar e retornar token")
    void login_deveRetornarToken_quandoCredenciaisValidas() {
        given(userRepository.findByEmail(loginDTO.getEmail())).willReturn(Optional.of(user));
        given(jwtService.generateToken(user)).willReturn("jwt_token");

        AuthResponseDTO response = authService.login(loginDTO);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getEmail()).isEqualTo("maria@email.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login - deve lançar exceção quando credenciais inválidas")
    void login_deveLancarExcecao_quandoCredenciaisInvalidas() {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }
}

