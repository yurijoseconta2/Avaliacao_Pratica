# API de Gerenciamento de Tarefas

API REST desenvolvida com Java 17+, Spring Boot, Spring Data JPA, H2, Bean Validation, JUnit 5 e Mockito.

## Executar

Requisitos: Java 17 ou superior e Maven 3.9+.

```bash
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080/api/tarefas`. O console H2 estará em `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:mem:testdb`, usuário `sa` e senha em branco.

## Testes

```bash
mvn test
```

## Endpoints

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/api/tarefas` | Cria tarefa (201) |
| GET | `/api/tarefas` | Lista tarefas (200) |
| GET | `/api/tarefas/{id}` | Busca por ID (200/404) |
| PUT | `/api/tarefas/{id}` | Atualiza tarefa (200/400/404) |
| DELETE | `/api/tarefas/{id}` | Exclui tarefa (204/400/404) |

Exemplo de criação:

```json
{
	"titulo": "Implementar login",
	"descricao": "Criar autenticação da equipe"
}
```

Os status aceitos são `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`. Para concluir uma tarefa, envie `dataConclusao` em formato ISO-8601, por exemplo `2026-09-29T14:30:00`. Uma tarefa pendente deve passar por `EM_ANDAMENTO` antes de ser concluída. Tarefas concluídas não podem ser excluídas.
