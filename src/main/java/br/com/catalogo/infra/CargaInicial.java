package br.com.catalogo.infra;

import br.com.catalogo.modelo.ItemCatalogo;
import br.com.catalogo.modelo.UsuarioSistema;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CargaInicial {

    @Transactional
    void carregar(@Observes StartupEvent event) {
        criarUsuario("Administrador", "admin", "123", "Admin");
        criarUsuario("Usuário Comum", "comum", "123", "Usuario");
        criarUsuario("Técnico Infra", "tecnico1", "123", "Tecnico");
        criarUsuario("Técnico Acessos", "tecnico2", "123", "Tecnico");

        criarItem("Infraestrutura: Provisionamento de Servidor vSphere",
                "Provisionamento, ajuste ou manutenção de servidores virtualizados.");
        criarItem("Acessos: Criação e Configuração de VPN Corporativa",
                "Solicitações de acesso remoto seguro para usuários corporativos.");
        criarItem("Hardware: Manutenção e Substituição de Ativos",
                "Atendimento para computadores, periféricos e equipamentos físicos.");
    }

    private void criarUsuario(String nome, String login, String senha, String perfil) {
        UsuarioSistema usuario = UsuarioSistema.find("login", login).firstResult();
        if (usuario != null) {
            usuario.nome = nome;
            usuario.senha = senha;
            usuario.perfil = perfil;
            usuario.ativo = true;
            return;
        }

        usuario = new UsuarioSistema();
        usuario.nome = nome;
        usuario.login = login;
        usuario.senha = senha;
        usuario.perfil = perfil;
        usuario.ativo = true;
        usuario.persist();
    }

    private void criarItem(String nome, String descricao) {
        ItemCatalogo item = ItemCatalogo.find("nome", nome).firstResult();
        if (item != null) {
            item.descricao = descricao;
            item.ativo = true;
            return;
        }

        item = new ItemCatalogo();
        item.nome = nome;
        item.descricao = descricao;
        item.ativo = true;
        item.persist();
    }
}
