package com.WagaClaude.wagaclaude.model;

import com.WagaClaude.wagaclaude.model.enums.TipoDisco;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "armazenamento")
@Getter
@Setter
@NoArgsConstructor
public class Armazenamento extends Recurso {

    @Column(nullable = false)
    private int capacidadeGB;

    @Column(nullable = false)
    private int usadoGB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDisco tipoDisco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vm_anexada_id")
    private VirtualMachine vmAnexada;

    public void expandir(int gb) {
        if (gb <= 0) {
            throw new IllegalArgumentException("A expansão deve ser maior que zero");
        }
        this.capacidadeGB += gb;
    }

    public void reduzir(int gb) {
        if (gb <= 0) {
            throw new IllegalArgumentException("A redução deve ser maior que zero");
        }
        if (this.capacidadeGB - gb < this.usadoGB) {
            throw new IllegalArgumentException(
                    "Não é possível reduzir abaixo do espaço já usado (" + usadoGB + " GB)");
        }
        this.capacidadeGB -= gb;
    }

    @Override
    public String getResumo() {
        String anexo = (vmAnexada != null) ? "anexado à VM '" + vmAnexada.getNome() + "'" : "solto";
        return String.format("Disco %s '%s' — %d/%d GB usados, %s [%s]",
                tipoDisco, getNome(), usadoGB, capacidadeGB, anexo, getStatus());
    }
}
