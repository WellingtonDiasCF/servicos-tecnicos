package br.com.catalogo.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class HistoricoSolicitacao extends PanacheEntity {
    public Long idSolicitacao;
    public String acao;
    public String usuarioResponsavel;
    public LocalDateTime dataHora;
}