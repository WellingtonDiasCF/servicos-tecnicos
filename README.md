# Serviços Técnicos

Projeto simples em Quarkus para controlar chamados técnicos.

A aplicação tem três tipos de usuário:

- Cliente: abre chamados.
- Admin: vê todos os chamados, cadastra técnicos e designa atendimentos.
- Técnico: vê apenas os chamados atribuídos a ele e pode concluir o atendimento.

O front é uma página HTML simples, e o backend expõe uma API REST com autenticação JWT.

## Tecnologias

- Java 21
- Quarkus
- PostgreSQL
- Docker Compose
- HTML, CSS e JavaScript

## Como rodar

Suba o banco:

```powershell
docker compose up -d
```

Rode a aplicação:

```powershell
.\mvnw.cmd quarkus:dev
```

Acesse:

```text
http://localhost:8080
```

## Usuários de teste

| Perfil | Login | Senha |
| --- | --- | --- |
| Admin | `admin` | `123` |
| Cliente | `comum` | `123` |
| Técnico | `tecnico1` | `123` |
| Técnico | `tecnico2` | `123` |

## Fluxo básico

1. Entrar como cliente e abrir um chamado.
2. Entrar como admin e designar o chamado para um técnico.
3. Entrar como técnico e concluir o chamado informando o que foi feito.

## Testando pelo Postman

Primeiro faça login:

```text
POST http://localhost:8080/auth/login
Body: x-www-form-urlencoded

usuario=admin
senha=123
```

A resposta é o token JWT. Use esse token nas próximas chamadas em:

```text
Authorization: Bearer <token>
```

Para testar ações de cliente ou técnico, faça login com o usuário correspondente e troque o token.

Criar técnico:

```text
POST http://localhost:8080/usuarios/tecnicos
```

```json
{
  "nome": "Técnico Postman",
  "login": "tecnico.postman",
  "senha": "123"
}
```

Abrir chamado:

```text
POST http://localhost:8080/solicitacoes
```

```json
{
  "idItemCatalogo": 1,
  "descricao": "Teste feito pelo Postman"
}
```

Designar chamado:

```text
PUT http://localhost:8080/solicitacoes/1/atribuir
```

```json
{
  "tecnicoLogin": "tecnico.postman"
}
```

Listar chamados do técnico:

```text
GET http://localhost:8080/solicitacoes/minhas?page=0&size=10
```

Concluir chamado:

```text
PUT http://localhost:8080/solicitacoes/1/concluir
```

```json
{
  "descricao": "Atendimento realizado e validado."
}
```
