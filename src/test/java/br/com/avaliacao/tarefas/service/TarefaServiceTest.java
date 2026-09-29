package br.com.avaliacao.tarefas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.avaliacao.tarefas.dto.TarefaRequest;
import br.com.avaliacao.tarefas.dto.TarefaResponse;
import br.com.avaliacao.tarefas.exception.RegraDeNegocioException;
import br.com.avaliacao.tarefas.exception.RecursoNaoEncontradoException;
import br.com.avaliacao.tarefas.model.StatusTarefa;
import br.com.avaliacao.tarefas.model.Tarefa;
import br.com.avaliacao.tarefas.repository.TarefaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    void deveCriarTarefaPendenteComDataDeCriacao() {
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa tarefa = invocation.getArgument(0);
            tarefa.setId(1L);
            tarefa.setDataCriacao(LocalDateTime.now());
            return tarefa;
        });

        TarefaResponse resposta = tarefaService.criar(
                new TarefaRequest("Implementar login", null, null, null));

        assertEquals(1L, resposta.id());
        assertEquals(StatusTarefa.PENDENTE, resposta.status());
        assertNotNull(resposta.dataCriacao());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    void deveListarTarefas() {
        Tarefa tarefa = tarefa(1L, "Revisar código", StatusTarefa.PENDENTE);
        when(tarefaRepository.findAll()).thenReturn(List.of(tarefa));

        List<TarefaResponse> resposta = tarefaService.listar();

        assertEquals(1, resposta.size());
        assertEquals("Revisar código", resposta.get(0).titulo());
    }

    @Test
    void deveBuscarTarefaPorId() {
        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa(1L, "Revisar código", StatusTarefa.PENDENTE)));

        assertEquals(1L, tarefaService.buscarPorId(1L).id());
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoExiste() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> tarefaService.buscarPorId(99L));
    }

    @Test
    void deveAtualizarTarefaEmAndamento() {
        Tarefa existente = tarefa(1L, "Revisar código", StatusTarefa.PENDENTE);
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TarefaResponse resposta = tarefaService.atualizar(1L,
                new TarefaRequest("Revisar pull request", "Prioridade alta",
                        StatusTarefa.EM_ANDAMENTO, null));

        assertEquals("Revisar pull request", resposta.titulo());
        assertEquals(StatusTarefa.EM_ANDAMENTO, resposta.status());
        assertEquals("Prioridade alta", resposta.descricao());
        verify(tarefaRepository).save(existente);
    }

    @Test
    void naoDevePermitirTransicaoDiretaDePendenteParaConcluida() {
        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa(1L, "Revisar código", StatusTarefa.PENDENTE)));

        assertThrows(RegraDeNegocioException.class,
                () -> tarefaService.atualizar(1L, new TarefaRequest(
                        "Revisar código", null, StatusTarefa.CONCLUIDA, LocalDateTime.now())));
    }

    @Test
    void deveExigirDataDeConclusao() {
        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa(1L, "Revisar código", StatusTarefa.EM_ANDAMENTO)));

        assertThrows(RegraDeNegocioException.class,
                () -> tarefaService.atualizar(1L, new TarefaRequest(
                        "Revisar código", null, StatusTarefa.CONCLUIDA, null)));
    }

    @Test
    void deveConcluirTarefaEmAndamentoComDataDeConclusao() {
        Tarefa existente = tarefa(1L, "Revisar código", StatusTarefa.EM_ANDAMENTO);
        LocalDateTime dataConclusao = LocalDateTime.of(2026, 9, 29, 14, 30);
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TarefaResponse resposta = tarefaService.atualizar(1L,
                new TarefaRequest("Revisar código", null, StatusTarefa.CONCLUIDA, dataConclusao));

        assertEquals(StatusTarefa.CONCLUIDA, resposta.status());
        assertEquals(dataConclusao, resposta.dataConclusao());
    }

    @Test
    void naoDeveCriarTituloDuplicadoEmTarefaAtiva() {
        when(tarefaRepository.existsByTituloIgnoreCaseAndStatusNot(
                "Revisar código", StatusTarefa.CONCLUIDA)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
                () -> tarefaService.criar(new TarefaRequest(
                        "Revisar código", null, StatusTarefa.PENDENTE, null)));
    }

    @Test
    void naoDeveAceitarTituloMenorQueCincoCaracteresAposRemoverEspacos() {
        assertThrows(RegraDeNegocioException.class,
                () -> tarefaService.criar(new TarefaRequest("    a", null, null, null)));
    }

    @Test
    void naoDeveExcluirTarefaConcluida() {
        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa(1L, "Revisar código", StatusTarefa.CONCLUIDA)));

        assertThrows(RegraDeNegocioException.class, () -> tarefaService.excluir(1L));
    }

    @Test
    void deveExcluirTarefaNaoConcluida() {
        Tarefa existente = tarefa(1L, "Revisar código", StatusTarefa.EM_ANDAMENTO);
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(existente));

        tarefaService.excluir(1L);

        verify(tarefaRepository).delete(existente);
    }

    private Tarefa tarefa(Long id, String titulo, StatusTarefa status) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setTitulo(titulo);
        tarefa.setStatus(status);
        tarefa.setDataCriacao(LocalDateTime.now());
        return tarefa;
    }
}