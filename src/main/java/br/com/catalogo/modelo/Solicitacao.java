package br.com.catalogo.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Solicitacao extends PanacheEntity {
    public String nomeUsuario;
    public Long idItemCatalogo;
    public String status;
    public String motivoRejeicao;
    public String descricao;
    public String tecnicoResponsavel;
    public String descricaoConclusao;
}
