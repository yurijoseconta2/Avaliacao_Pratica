package br.com.avaliacao.tarefas.service;

import br.com.avaliacao.tarefas.dto.TarefaRequest;
import br.com.avaliacao.tarefas.dto.TarefaResponse;
import br.com.avaliacao.tarefas.exception.RegraDeNegocioException;
import br.com.avaliacao.tarefas.exception.RecursoNaoEncontradoException;
import br.com.avaliacao.tarefas.model.StatusTarefa;
import br.com.avaliacao.tarefas.model.Tarefa;
import br.com.avaliacao.tarefas.repository.TarefaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public TarefaResponse criar(TarefaRequest request) {
        String titulo = normalizarTitulo(request.titulo());
        StatusTarefa status = request.status() == null
                ? StatusTarefa.PENDENTE
                : request.status();
        validarDataConclusao(status, request.dataConclusao());
        validarTituloAtivo(titulo, status);

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(request.descricao());
        tarefa.setStatus(status);
        tarefa.setDataConclusao(request.dataConclusao());
        return paraResponse(tarefaRepository.save(tarefa));
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listar() {
        return tarefaRepository.findAll().stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa tarefa = buscarEntidade(id);
        String titulo = normalizarTitulo(request.titulo());
        if (request.status() == null) {
            throw new RegraDeNegocioException("O status é obrigatório na atualização");
        }
        if (tarefa.getStatus() == StatusTarefa.PENDENTE
                && request.status() == StatusTarefa.CONCLUIDA) {
            throw new RegraDeNegocioException(
                    "Uma tarefa pendente deve passar por EM_ANDAMENTO antes de ser concluída");
        }

        validarDataConclusao(request.status(), request.dataConclusao());
        validarTituloAtivo(id, titulo, request.status());

        tarefa.setTitulo(titulo);
        tarefa.setDescricao(request.descricao());
        tarefa.setStatus(request.status());
        tarefa.setDataConclusao(request.status() == StatusTarefa.CONCLUIDA
                ? request.dataConclusao()
                : null);
        return paraResponse(tarefaRepository.save(tarefa));
    }

    public void excluir(Long id) {
        Tarefa tarefa = buscarEntidade(id);
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            throw new RegraDeNegocioException("Tarefas concluídas não podem ser excluídas");
        }
        tarefaRepository.delete(tarefa);
    }

    private Tarefa buscarEntidade(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tarefa não encontrada com o id " + id));
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null) {
            throw new RegraDeNegocioException("O título é obrigatório");
        }
        String normalizado = titulo.strip();
        if (normalizado.length() < 5 || normalizado.length() > 100) {
            throw new RegraDeNegocioException(
                    "O título deve ter entre 5 e 100 caracteres, desconsiderando espaços externos");
        }
        return normalizado;
    }

    private void validarTituloAtivo(String titulo, StatusTarefa status) {
        if (status != StatusTarefa.CONCLUIDA
                && tarefaRepository.existsByTituloIgnoreCaseAndStatusNot(
                        titulo, StatusTarefa.CONCLUIDA)) {
            throw new RegraDeNegocioException("Já existe uma tarefa ativa com esse título");
        }
    }

    private void validarTituloAtivo(Long id, String titulo, StatusTarefa status) {
        if (status != StatusTarefa.CONCLUIDA
                && tarefaRepository.existsByTituloIgnoreCaseAndStatusNotAndIdNot(
                titulo, StatusTarefa.CONCLUIDA, id)) {
            throw new RegraDeNegocioException("Já existe uma tarefa ativa com esse título");
        }
    }

    private void validarDataConclusao(StatusTarefa status, LocalDateTime dataConclusao) {
        if (status == StatusTarefa.CONCLUIDA && dataConclusao == null) {
            throw new RegraDeNegocioException(
                    "A dataConclusao é obrigatória para tarefas concluídas");
        }
    }

    private TarefaResponse paraResponse(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getDataCriacao(),
                tarefa.getDataConclusao());
    }
}