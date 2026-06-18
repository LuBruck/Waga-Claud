package com.WagaCloud.wagacloud.repository;

import com.WagaCloud.wagacloud.model.Armazenamento;
import com.WagaCloud.wagacloud.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmazenamentoRepository extends JpaRepository<Armazenamento, Integer> {

    List<Armazenamento> findByDono(Usuario dono);

    List<Armazenamento> findByVmAnexadaIsNull();
}
