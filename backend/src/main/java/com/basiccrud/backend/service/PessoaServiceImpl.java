package com.basiccrud.backend.service;

import com.basiccrud.backend.dto.PessoaRequestDTO;
import com.basiccrud.backend.dto.PessoaResponseDTO;
import com.basiccrud.backend.exception.EmailAlreadyExistsException;
import com.basiccrud.backend.exception.PessoaNotFoundException;
import com.basiccrud.backend.model.Pessoa;
import com.basiccrud.backend.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PessoaServiceImpl implements PessoaService {

    private final PessoaRepository pessoaRepository;

    @Override
    @Transactional
    public PessoaResponseDTO criar(PessoaRequestDTO dto) {
        log.info("Criando pessoa com email: {}", dto.getEmail());

        if (pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        Pessoa pessoa = Pessoa.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .dataNascimento(dto.getDataNascimento())
                .build();

        Pessoa salva = pessoaRepository.save(pessoa);
        log.info("Pessoa criada com id: {}", salva.getId());
        return PessoaResponseDTO.fromEntity(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public PessoaResponseDTO buscarPorId(UUID id) {
        log.info("Buscando pessoa com id: {}", id);
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNotFoundException(id));
        return PessoaResponseDTO.fromEntity(pessoa);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PessoaResponseDTO> listar(Pageable pageable) {
        log.info("Listando pessoas - página: {}, tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        return pessoaRepository.findAll(pageable).map(PessoaResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public PessoaResponseDTO atualizar(UUID id, PessoaRequestDTO dto) {
        log.info("Atualizando pessoa com id: {}", id);

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNotFoundException(id));

        if (!pessoa.getEmail().equals(dto.getEmail()) && pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        pessoa.setNome(dto.getNome());
        pessoa.setEmail(dto.getEmail());
        pessoa.setDataNascimento(dto.getDataNascimento());

        Pessoa atualizada = pessoaRepository.save(pessoa);
        log.info("Pessoa atualizada com id: {}", atualizada.getId());
        return PessoaResponseDTO.fromEntity(atualizada);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando pessoa com id: {}", id);
        if (!pessoaRepository.existsById(id)) {
            throw new PessoaNotFoundException(id);
        }
        pessoaRepository.deleteById(id);
        log.info("Pessoa deletada com id: {}", id);
    }
}

