package br.com.catalogo.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class UsuarioSistema extends PanacheEntity {
    public String nome;

    @Column(unique = true, nullable = false)
    public String login;

    @Column(nullable = false)
    public String senha;

    @Column(nullable = false)
    public String perfil;

    public boolean ativo = true;
}
