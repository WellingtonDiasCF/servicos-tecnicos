package br.com.catalogo.recurso;

import br.com.catalogo.modelo.UsuarioSistema;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import java.util.Set;

@Path("/auth")
public class AutenticacaoRecurso {

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    public String login(@FormParam("usuario") String usuario, @FormParam("senha") String senha) {
        if (usuario == null || senha == null) {
            throw new WebApplicationException("Usuário e senha são obrigatórios.", 400);
        }

        UsuarioSistema usuarioSistema = UsuarioSistema.find("login", usuario.trim()).firstResult();
        if (usuarioSistema == null || !usuarioSistema.ativo || !senha.equals(usuarioSistema.senha)) {
            throw new WebApplicationException("Credenciais inválidas.", 401);
        }

        return Jwt.issuer("https://meucatalogo.com/issuer")
                .upn(usuarioSistema.login)
                .claim("nome", usuarioSistema.nome)
                .groups(Set.of(usuarioSistema.perfil))
                .sign();
    }
}
