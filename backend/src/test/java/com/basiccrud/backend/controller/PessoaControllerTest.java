package com.basiccrud.backend.controller;

import com.basiccrud.backend.dto.PessoaLoteRequestDTO;
import com.basiccrud.backend.dto.PessoaRequestDTO;
import com.basiccrud.backend.dto.PessoaResponseDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.exception.PessoaNotFoundException;
import com.basiccrud.backend.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PessoaController - Testes de Integração")
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PessoaService pessoaService;

    private UUID id;
    private PessoaResponseDTO responseDTO;
    private PessoaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        responseDTO = PessoaResponseDTO.builder()
                .id(id)
                .nome("João Silva")
                .email("joao@email.com")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .build();

        requestDTO = PessoaRequestDTO.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoa - deve criar pessoa e retornar 201")
    void criar_deveRetornar201_quandoDadosValidos() throws Exception {
        given(pessoaService.criar(any())).willReturn(responseDTO);

        mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoa - deve retornar 400 quando dados inválidos")
    void criar_deveRetornar400_quandoDadosInvalidos() throws Exception {
        PessoaRequestDTO invalid = PessoaRequestDTO.builder()
                .nome("")
                .email("email-invalido")
                .dataNascimento(null)
                .build();

        mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoa - deve retornar 409 quando email já existe")
    void criar_deveRetornar409_quandoEmailDuplicado() throws Exception {
        given(pessoaService.criar(any())).willThrow(new EmailAlreadyExistsException("joao@email.com"));

        mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /pessoa/{id} - deve retornar 200 com pessoa")
    void buscarPorId_deveRetornar200_quandoIdExiste() throws Exception {
        given(pessoaService.buscarPorId(id)).willReturn(responseDTO);

        mockMvc.perform(get("/pessoa/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /pessoa/{id} - deve retornar 404 quando não encontrado")
    void buscarPorId_deveRetornar404_quandoIdNaoExiste() throws Exception {
        given(pessoaService.buscarPorId(id)).willThrow(new PessoaNotFoundException(id));

        mockMvc.perform(get("/pessoa/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /pessoas - deve retornar 200 com página de pessoas")
    void listar_deveRetornar200_comPaginacao() throws Exception {
        given(pessoaService.listar(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(responseDTO)));

        mockMvc.perform(get("/pessoas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("joao@email.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /pessoa/{id} - deve retornar 200 com pessoa atualizada")
    void atualizar_deveRetornar200_quandoDadosValidos() throws Exception {
        given(pessoaService.atualizar(eq(id), any())).willReturn(responseDTO);

        mockMvc.perform(put("/pessoa/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /pessoa/{id} - deve retornar 204")
    void deletar_deveRetornar204_quandoIdExiste() throws Exception {
        doNothing().when(pessoaService).deletar(id);

        mockMvc.perform(delete("/pessoa/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /pessoa/{id} - deve retornar 404 quando não encontrado")
    void deletar_deveRetornar404_quandoIdNaoExiste() throws Exception {
        doThrow(new PessoaNotFoundException(id)).when(pessoaService).deletar(id);

        mockMvc.perform(delete("/pessoa/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Qualquer endpoint sem autenticação deve retornar 403")
    void semAutenticacao_deveRetornar403() throws Exception {
        mockMvc.perform(get("/pessoa/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoas/lote - deve criar pessoas e retornar 201")
    void criarLote_deveRetornar201_quandoDadosValidos() throws Exception {
        PessoaRequestDTO dto2 = PessoaRequestDTO.builder()
                .nome("Maria Souza")
                .email("maria@email.com")
                .dataNascimento(LocalDate.of(1995, 6, 10))
                .build();

        PessoaResponseDTO responseDTO2 = PessoaResponseDTO.builder()
                .id(UUID.randomUUID())
                .nome("Maria Souza")
                .email("maria@email.com")
                .dataNascimento(LocalDate.of(1995, 6, 10))
                .build();

        given(pessoaService.criarLote(any())).willReturn(List.of(responseDTO, responseDTO2));

        PessoaLoteRequestDTO body = new PessoaLoteRequestDTO(List.of(requestDTO, dto2));

        mockMvc.perform(post("/pessoas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].email").value("maria@email.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoas/lote - deve retornar 400 quando lista está vazia")
    void criarLote_deveRetornar400_quandoListaVazia() throws Exception {
        PessoaLoteRequestDTO body = new PessoaLoteRequestDTO(List.of());

        mockMvc.perform(post("/pessoas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pessoas/lote - deve retornar 409 quando email já existe")
    void criarLote_deveRetornar409_quandoEmailDuplicado() throws Exception {
        given(pessoaService.criarLote(any())).willThrow(new EmailAlreadyExistsException("joao@email.com"));

        PessoaLoteRequestDTO body = new PessoaLoteRequestDTO(List.of(requestDTO));

        mockMvc.perform(post("/pessoas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}

