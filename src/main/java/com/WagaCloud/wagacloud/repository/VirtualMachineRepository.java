package com.WagaCloud.wagacloud.repository;

import com.WagaCloud.wagacloud.model.VirtualMachine;
import com.WagaCloud.wagacloud.model.Usuario;
import com.WagaCloud.wagacloud.model.enums.StatusRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Integer> {

    List<VirtualMachine> findByDono(Usuario dono);

    List<VirtualMachine> findByStatus(StatusRecurso status);
}
