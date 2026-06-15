package com.WagaClaude.wagaclaude.model;

import com.WagaClaude.wagaclaude.model.enums.StatusRecurso;
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
