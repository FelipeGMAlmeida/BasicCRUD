package com.basiccrud.backend.service;

import com.basiccrud.backend.dto.AuthResponseDTO;
import com.basiccrud.backend.dto.LoginRequestDTO;
import com.basiccrud.backend.dto.RegisterRequestDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.model.User;
import com.basiccrud.backend.repository.UserRepository;
import com.basiccrud.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        log.info("Registrando novo usuário: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        return AuthResponseDTO.of(token, user.getEmail(), user.getName());
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        log.info("Autenticando usuário: {}", dto.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);
        log.info("Login realizado com sucesso: {}", user.getEmail());
        return AuthResponseDTO.of(token, user.getEmail(), user.getName());
    }
}

