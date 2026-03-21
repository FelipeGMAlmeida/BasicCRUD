package com.basiccrud.backend.controller;

import com.basiccrud.backend.dto.PessoaRequestDTO;
import com.basiccrud.backend.dto.PessoaResponseDTO;
import com.basiccrud.backend.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping("/pessoa")
    public ResponseEntity<PessoaResponseDTO> criar(@Valid @RequestBody PessoaRequestDTO dto) {
        log.info("POST /pessoa");
        PessoaResponseDTO response = pessoaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pessoa/{id}")
    public ResponseEntity<PessoaResponseDTO> buscarPorId(@PathVariable UUID id) {
        log.info("GET /pessoa/{}", id);
        return ResponseEntity.ok(pessoaService.buscarPorId(id));
    }

    @GetMapping("/pessoas")
    public ResponseEntity<Page<PessoaResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        log.info("GET /pessoas");
        return ResponseEntity.ok(pessoaService.listar(pageable));
    }

    @PutMapping("/pessoa/{id}")
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PessoaRequestDTO dto) {
        log.info("PUT /pessoa/{}", id);
        return ResponseEntity.ok(pessoaService.atualizar(id, dto));
    }

    @DeleteMapping("/pessoa/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        log.info("DELETE /pessoa/{}", id);
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

