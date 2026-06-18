package com.WagaCloud.wagacloud.model;

import com.WagaCloud.wagacloud.model.enums.StatusRecurso;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "virtual_machine")
@Getter
@Setter
@NoArgsConstructor
public class VirtualMachine extends Recurso {

    @Column(nullable = false)
    private int qtdCpu;

    @Column(nullable = false)
    private int memoriaRamGB;

    @Column(nullable = false)
    private String so;

    @JsonIgnore
    @OneToMany(mappedBy = "vmAnexada")
    private List<Armazenamento> discos = new ArrayList<>();

    public void iniciar() {
        setStatus(StatusRecurso.ATIVO);
    }

    public void parar() {
        setStatus(StatusRecurso.PARADO);
    }

    @Override
    public String getResumo() {
        return String.format("VM '%s' — %d vCPU, %d GB RAM, %s [%s]",
                getNome(), qtdCpu, memoriaRamGB, so, getStatus());
    }
}
