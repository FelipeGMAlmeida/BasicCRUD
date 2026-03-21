package com.basiccrud.backend.service;

import com.basiccrud.backend.dto.PessoaRequestDTO;
import com.basiccrud.backend.dto.PessoaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PessoaService {

    PessoaResponseDTO criar(PessoaRequestDTO dto);

    PessoaResponseDTO buscarPorId(UUID id);

    Page<PessoaResponseDTO> listar(Pageable pageable);

    PessoaResponseDTO atualizar(UUID id, PessoaRequestDTO dto);

    void deletar(UUID id);
}

