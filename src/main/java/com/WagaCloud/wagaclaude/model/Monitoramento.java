package com.WagaCloud.wagacloud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "monitoramento")
@Getter
@Setter
@NoArgsConstructor
public class Monitoramento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @Column(nullable = false)
    private String metrica;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public boolean isCritico() {
        return this.valor >= 95.0;
    }

    public String getValorFormatado() {
        return String.format("%.1f%%", this.valor);
    }
}
