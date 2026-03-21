package com.basiccrud.backend.repository;

import com.basiccrud.backend.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    Optional<Pessoa> findByEmail(String email);

    boolean existsByEmail(String email);
}

