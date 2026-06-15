package com.WagaClaude.wagaclaude.repository;

import com.WagaClaude.wagaclaude.model.Armazenamento;
import com.WagaClaude.wagaclaude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmazenamentoRepository extends JpaRepository<Armazenamento, Integer> {

    List<Armazenamento> findByDono(Usuario dono);

    List<Armazenamento> findByVmAnexadaIsNull();
}
