package com.WagaCloud.wagaclaude.repository;

import com.WagaCloud.wagaclaude.model.Armazenamento;
import com.WagaCloud.wagaclaude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmazenamentoRepository extends JpaRepository<Armazenamento, Integer> {

    List<Armazenamento> findByDono(Usuario dono);

    List<Armazenamento> findByVmAnexadaIsNull();
}
