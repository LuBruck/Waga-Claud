package com.WagaClaude.wagaclaude.repository;

import com.WagaClaude.wagaclaude.model.VirtualMachine;
import com.WagaClaude.wagaclaude.model.Usuario;
import com.WagaClaude.wagaclaude.model.enums.StatusRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Integer> {

    List<VirtualMachine> findByDono(Usuario dono);

    List<VirtualMachine> findByStatus(StatusRecurso status);
}
