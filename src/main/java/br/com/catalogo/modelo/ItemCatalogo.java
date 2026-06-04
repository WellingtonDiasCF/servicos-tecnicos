package br.com.catalogo.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class ItemCatalogo extends PanacheEntity {
    public String nome;
    public String descricao;
    public boolean ativo;
}