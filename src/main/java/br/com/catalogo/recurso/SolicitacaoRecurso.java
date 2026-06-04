package br.com.catalogo.recurso;

import br.com.catalogo.dto.AtribuicaoRequest;
import br.com.catalogo.dto.ConclusaoRequest;
import br.com.catalogo.dto.PaginaResposta;
import br.com.catalogo.dto.RejeicaoRequest;
import br.com.catalogo.modelo.HistoricoSolicitacao;
import br.com.catalogo.modelo.Solicitacao;
import br.com.catalogo.modelo.UsuarioSistema;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;

import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/solicitacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolicitacaoRecurso {

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("Admin")
    public Response listarTodas(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("status") String status) {

        PanacheQuery<Solicitacao> query = criarQuery(status, null);
        return Response.ok(paginar(query, page, size)).build();
    }

    @GET
    @Path("/minhas")
    @RolesAllowed("Tecnico")
    public Response listarMinhas(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("status") String status) {

        PanacheQuery<Solicitacao> query = criarQuery(status, jwt.getName());
        return Response.ok(paginar(query, page, size)).build();
    }

    @POST
    @RolesAllowed({"Usuario", "Admin"})
    @Transactional
    public Response criar(Solicitacao solicitacao) {
        if (solicitacao == null || solicitacao.descricao == null || solicitacao.descricao.trim().isEmpty()) {
            throw new WebApplicationException("Descrição do chamado é obrigatória.", 400);
        }

        solicitacao.id = null;
        solicitacao.status = "PENDENTE";
        solicitacao.nomeUsuario = jwt.getName();
        solicitacao.tecnicoResponsavel = null;
        solicitacao.motivoRejeicao = null;
        solicitacao.descricaoConclusao = null;
        solicitacao.persist();

        registrarHistorico(solicitacao.id, "CRIACAO", jwt.getName());
        return Response.status(Response.Status.CREATED).entity(solicitacao).build();
    }

    @PUT
    @Path("/{id}/rejeitar")
    @RolesAllowed("Admin")
    @Transactional
    public Response rejeitar(@PathParam("id") Long id, RejeicaoRequest request) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (!"PENDENTE".equals(solicitacao.status)) {
            throw new WebApplicationException("Apenas solicitações PENDENTES podem ser rejeitadas.", 400);
        }

        if (request == null || request.motivo == null || request.motivo.trim().isEmpty()) {
            throw new WebApplicationException("O motivo da rejeição é obrigatório.", 400);
        }

        solicitacao.status = "REJEITADA";
        solicitacao.motivoRejeicao = request.motivo.trim();
        registrarHistorico(solicitacao.id, "REJEICAO", jwt.getName());
        return Response.ok(solicitacao).build();
    }

    @PUT
    @Path("/{id}/atribuir")
    @RolesAllowed("Admin")
    @Transactional
    public Response atribuir(@PathParam("id") Long id, AtribuicaoRequest request) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (!"PENDENTE".equals(solicitacao.status)) {
            throw new WebApplicationException("Apenas solicitações PENDENTES podem ser designadas.", 400);
        }

        if (request == null || request.tecnicoLogin == null || request.tecnicoLogin.trim().isEmpty()) {
            throw new WebApplicationException("Informe o técnico responsável.", 400);
        }

        UsuarioSistema tecnico = UsuarioSistema.find("login = ?1 and perfil = ?2 and ativo = true",
                request.tecnicoLogin.trim(), "Tecnico").firstResult();
        if (tecnico == null) {
            throw new WebApplicationException("Técnico ativo não encontrado.", 404);
        }

        solicitacao.status = "EM_ATENDIMENTO";
        solicitacao.tecnicoResponsavel = tecnico.login;
        registrarHistorico(solicitacao.id, "ATRIBUICAO:" + tecnico.login, jwt.getName());
        return Response.ok(solicitacao).build();
    }

    @PUT
    @Path("/{id}/concluir")
    @RolesAllowed({"Admin", "Tecnico"})
    @Transactional
    public Response concluir(@PathParam("id") Long id, ConclusaoRequest request) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (!"EM_ATENDIMENTO".equals(solicitacao.status)) {
            throw new WebApplicationException("O chamado precisa estar EM_ATENDIMENTO para ser concluído.", 400);
        }

        if (request == null || request.descricao == null || request.descricao.trim().isEmpty()) {
            throw new WebApplicationException("Informe o que foi feito antes de concluir o chamado.", 400);
        }

        String usuarioLogado = jwt.getName();
        boolean isAdmin = jwt.getGroups().contains("Admin");
        if (!isAdmin && !usuarioLogado.equals(solicitacao.tecnicoResponsavel)) {
            throw new WebApplicationException("Apenas o técnico responsável ou um Admin pode concluir este chamado.", 403);
        }

        solicitacao.status = "CONCLUIDA";
        solicitacao.descricaoConclusao = request.descricao.trim();
        registrarHistorico(solicitacao.id, "CONCLUSAO:" + solicitacao.descricaoConclusao, usuarioLogado);
        return Response.ok(solicitacao).build();
    }

    private PanacheQuery<Solicitacao> criarQuery(String status, String tecnicoResponsavel) {
        boolean filtrarStatus = status != null && !status.trim().isEmpty();
        boolean filtrarTecnico = tecnicoResponsavel != null && !tecnicoResponsavel.trim().isEmpty();

        if (filtrarStatus && filtrarTecnico) {
            return Solicitacao.find("status = ?1 and tecnicoResponsavel = ?2 order by id desc",
                    status.trim().toUpperCase(), tecnicoResponsavel);
        }

        if (filtrarStatus) {
            return Solicitacao.find("status = ?1 order by id desc", status.trim().toUpperCase());
        }

        if (filtrarTecnico) {
            return Solicitacao.find("tecnicoResponsavel = ?1 order by id desc", tecnicoResponsavel);
        }

        return Solicitacao.find("order by id desc");
    }

    private PaginaResposta<Solicitacao> paginar(PanacheQuery<Solicitacao> query, int page, int size) {
        int pagina = Math.max(page, 0);
        int tamanho = Math.max(size, 1);
        query.page(Page.of(pagina, tamanho));

        return new PaginaResposta<>(
                query.list(),
                pagina,
                query.pageCount(),
                query.count());
    }

    private Solicitacao buscarSolicitacao(Long id) {
        Solicitacao solicitacao = Solicitacao.findById(id);
        if (solicitacao == null) {
            throw new WebApplicationException("Solicitação não encontrada.", 404);
        }
        return solicitacao;
    }

    private void registrarHistorico(Long idSolicitacao, String acao, String usuarioResponsavel) {
        HistoricoSolicitacao historico = new HistoricoSolicitacao();
        historico.idSolicitacao = idSolicitacao;
        historico.acao = acao;
        historico.usuarioResponsavel = usuarioResponsavel;
        historico.dataHora = LocalDateTime.now();
        historico.persist();
    }
}
