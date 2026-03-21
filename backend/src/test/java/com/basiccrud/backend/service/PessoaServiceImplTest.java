package com.basiccrud.backend.service;

import com.basiccrud.backend.dto.PessoaRequestDTO;
import com.basiccrud.backend.dto.PessoaResponseDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.exception.PessoaNotFoundException;
import com.basiccrud.backend.model.Pessoa;
import com.basiccrud.backend.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PessoaServiceImpl - Testes Unitários")
class PessoaServiceImplTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private PessoaServiceImpl pessoaService;

    private Pessoa pessoa;
    private PessoaRequestDTO requestDTO;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        pessoa = Pessoa.builder()
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
    @DisplayName("criar - deve criar pessoa com sucesso")
    void criar_deveRetornarPessoa_quandoDadosValidos() {
        given(pessoaRepository.existsByEmail(requestDTO.getEmail())).willReturn(false);
        given(pessoaRepository.save(any(Pessoa.class))).willReturn(pessoa);

        PessoaResponseDTO response = pessoaService.criar(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("criar - deve lançar exceção quando email já existe")
    void criar_deveLancarExcecao_quandoEmailJaExiste() {
        given(pessoaRepository.existsByEmail(requestDTO.getEmail())).willReturn(true);

        assertThatThrownBy(() -> pessoaService.criar(requestDTO))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(pessoaRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId - deve retornar pessoa quando id existe")
    void buscarPorId_deveRetornarPessoa_quandoIdExiste() {
        given(pessoaRepository.findById(id)).willReturn(Optional.of(pessoa));

        PessoaResponseDTO response = pessoaService.buscarPorId(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("buscarPorId - deve lançar exceção quando id não existe")
    void buscarPorId_deveLancarExcecao_quandoIdNaoExiste() {
        given(pessoaRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pessoaService.buscarPorId(id))
                .isInstanceOf(PessoaNotFoundException.class);
    }

    @Test
    @DisplayName("listar - deve retornar página de pessoas")
    void listar_deveRetornarPagina() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Pessoa> page = new PageImpl<>(List.of(pessoa));
        given(pessoaRepository.findAll(pageable)).willReturn(page);

        Page<PessoaResponseDTO> result = pessoaService.listar(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("atualizar - deve atualizar pessoa com sucesso")
    void atualizar_deveRetornarPessoaAtualizada_quandoDadosValidos() {
        PessoaRequestDTO updateDTO = PessoaRequestDTO.builder()
                .nome("João Atualizado")
                .email("joao@email.com")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .build();

        given(pessoaRepository.findById(id)).willReturn(Optional.of(pessoa));
        given(pessoaRepository.save(any(Pessoa.class))).willReturn(pessoa);

        PessoaResponseDTO response = pessoaService.atualizar(id, updateDTO);

        assertThat(response).isNotNull();
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("atualizar - deve lançar exceção quando pessoa não existe")
    void atualizar_deveLancarExcecao_quandoPessoaNaoExiste() {
        given(pessoaRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pessoaService.atualizar(id, requestDTO))
                .isInstanceOf(PessoaNotFoundException.class);
    }

    @Test
    @DisplayName("atualizar - deve lançar exceção quando novo email já pertence a outra pessoa")
    void atualizar_deveLancarExcecao_quandoNovoEmailJaExiste() {
        PessoaRequestDTO updateDTO = PessoaRequestDTO.builder()
                .nome("João Silva")
                .email("outro@email.com")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .build();

        given(pessoaRepository.findById(id)).willReturn(Optional.of(pessoa));
        given(pessoaRepository.existsByEmail("outro@email.com")).willReturn(true);

        assertThatThrownBy(() -> pessoaService.atualizar(id, updateDTO))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("deletar - deve deletar pessoa com sucesso")
    void deletar_deveDeletar_quandoIdExiste() {
        given(pessoaRepository.existsById(id)).willReturn(true);

        pessoaService.deletar(id);

        verify(pessoaRepository).deleteById(id);
    }

    @Test
    @DisplayName("deletar - deve lançar exceção quando pessoa não existe")
    void deletar_deveLancarExcecao_quandoIdNaoExiste() {
        given(pessoaRepository.existsById(id)).willReturn(false);

        assertThatThrownBy(() -> pessoaService.deletar(id))
                .isInstanceOf(PessoaNotFoundException.class);

        verify(pessoaRepository, never()).deleteById(any());
    }
}

