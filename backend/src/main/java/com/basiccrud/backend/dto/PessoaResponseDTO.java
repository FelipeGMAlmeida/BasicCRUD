package com.basiccrud.backend.dto;

import com.basiccrud.backend.model.Pessoa;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class PessoaResponseDTO {

    private UUID id;
    private String nome;
    private String email;
    private LocalDate dataNascimento;

    public static PessoaResponseDTO fromEntity(Pessoa pessoa) {
        return PessoaResponseDTO.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .email(pessoa.getEmail())
                .dataNascimento(pessoa.getDataNascimento())
                .build();
    }
}

