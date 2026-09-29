package br.com.avaliacao.tarefas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.avaliacao.tarefas.dto.TarefaResponse;
import br.com.avaliacao.tarefas.exception.ApiExceptionHandler;
import br.com.avaliacao.tarefas.exception.RecursoNaoEncontradoException;
import br.com.avaliacao.tarefas.model.StatusTarefa;
import br.com.avaliacao.tarefas.service.TarefaService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(TarefaController.class)
@Import(ApiExceptionHandler.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
    private TarefaService tarefaService;

    @Test
    void deveCriarTarefaERetornar201() throws Exception {
        when(tarefaService.criar(any())).thenReturn(resposta(1L, "Implementar login"));

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Implementar login","descricao":"Autenticação"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Implementar login"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRejeitarTituloInvalido() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"API"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveListarTarefas() throws Exception {
        when(tarefaService.listar()).thenReturn(List.of(resposta(1L, "Implementar login")));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Implementar login"));
    }

    @Test
    void deveRetornar404QuandoTarefaNaoExiste() throws Exception {
        when(tarefaService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Tarefa não encontrada com o id 99"));

        mockMvc.perform(get("/api/tarefas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveAtualizarTarefa() throws Exception {
        when(tarefaService.atualizar(eq(1L), any()))
                .thenReturn(resposta(1L, "Revisar pull request"));

        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Revisar pull request","status":"EM_ANDAMENTO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Revisar pull request"));
    }

    private TarefaResponse resposta(Long id, String titulo) {
        return new TarefaResponse(id, titulo, null, StatusTarefa.PENDENTE,
                LocalDateTime.of(2026, 9, 29, 10, 0), null);
    }
}