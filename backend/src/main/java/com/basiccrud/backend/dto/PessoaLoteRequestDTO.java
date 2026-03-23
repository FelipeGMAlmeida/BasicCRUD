package com.basiccrud.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PessoaLoteRequestDTO {

    @NotEmpty(message = "A lista de pessoas não pode ser vazia")
    @Valid
    private List<PessoaRequestDTO> pessoas;
}

