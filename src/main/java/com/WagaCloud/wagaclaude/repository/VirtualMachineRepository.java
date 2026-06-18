package com.WagaCloud.wagaclaude.repository;

import com.WagaCloud.wagaclaude.model.VirtualMachine;
import com.WagaCloud.wagaclaude.model.Usuario;
import com.WagaCloud.wagaclaude.model.enums.StatusRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Integer> {

    List<VirtualMachine> findByDono(Usuario dono);

    List<VirtualMachine> findByStatus(StatusRecurso status);
}
