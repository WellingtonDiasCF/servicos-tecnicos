package br.com.catalogo.recurso;

import br.com.catalogo.dto.TecnicoRequest;
import br.com.catalogo.modelo.Solicitacao;
import br.com.catalogo.modelo.UsuarioSistema;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/usuarios/tecnicos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TecnicoRecurso {

    @GET
    @RolesAllowed("Admin")
    public List<UsuarioSistema> listarTecnicos() {
        return UsuarioSistema.list("perfil = ?1 and ativo = true order by nome", "Tecnico");
    }

    @POST
    @RolesAllowed("Admin")
    @Transactional
    public Response criarTecnico(TecnicoRequest request) {
        validarTecnico(request, true);

        String login = request.login.trim();
        if (buscarPorLogin(login) != null) {
            throw new WebApplicationException("Já existe um usuário com este login.", 409);
        }

        UsuarioSistema tecnico = new UsuarioSistema();
        tecnico.nome = request.nome.trim();
        tecnico.login = login;
        tecnico.senha = request.senha;
        tecnico.perfil = "Tecnico";
        tecnico.ativo = true;
        tecnico.persist();

        return Response.status(Response.Status.CREATED).entity(tecnico).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("Admin")
    @Transactional
    public Response editarTecnico(@PathParam("id") Long id, TecnicoRequest request) {
        validarTecnico(request, false);

        UsuarioSistema tecnico = buscarTecnico(id);
        String loginAtual = tecnico.login;
        String novoLogin = request.login.trim();

        UsuarioSistema usuarioComMesmoLogin = buscarPorLogin(novoLogin);
        if (usuarioComMesmoLogin != null && !usuarioComMesmoLogin.id.equals(tecnico.id)) {
            throw new WebApplicationException("Já existe um usuário com este login.", 409);
        }

        tecnico.nome = request.nome.trim();
        tecnico.login = novoLogin;
        if (!textoVazio(request.senha)) {
            tecnico.senha = request.senha;
        }

        if (!loginAtual.equals(novoLogin)) {
            Solicitacao.update("tecnicoResponsavel = ?1 where tecnicoResponsavel = ?2", novoLogin, loginAtual);
        }

        return Response.ok(tecnico).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("Admin")
    @Transactional
    public Response excluirTecnico(@PathParam("id") Long id) {
        UsuarioSistema tecnico = buscarTecnico(id);

        long chamadosEmAtendimento = Solicitacao.count(
                "tecnicoResponsavel = ?1 and status = ?2",
                tecnico.login,
                "EM_ATENDIMENTO");
        if (chamadosEmAtendimento > 0) {
            throw new WebApplicationException("Não é possível excluir técnico com chamado em atendimento.", 400);
        }

        tecnico.ativo = false;
        return Response.noContent().build();
    }

    private UsuarioSistema buscarTecnico(Long id) {
        UsuarioSistema tecnico = UsuarioSistema.findById(id);
        if (tecnico == null || !"Tecnico".equals(tecnico.perfil) || !tecnico.ativo) {
            throw new WebApplicationException("Técnico ativo não encontrado.", 404);
        }
        return tecnico;
    }

    private UsuarioSistema buscarPorLogin(String login) {
        return UsuarioSistema.find("login", login).firstResult();
    }

    private void validarTecnico(TecnicoRequest request, boolean exigirSenha) {
        boolean dadosBasicosInvalidos = request == null
                || textoVazio(request.nome)
                || textoVazio(request.login);
        boolean senhaObrigatoriaNaoInformada = exigirSenha
                && (request == null || textoVazio(request.senha));

        if (dadosBasicosInvalidos || senhaObrigatoriaNaoInformada) {
            throw new WebApplicationException("Nome, login e senha são obrigatórios.", 400);
        }
    }

    private boolean textoVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
