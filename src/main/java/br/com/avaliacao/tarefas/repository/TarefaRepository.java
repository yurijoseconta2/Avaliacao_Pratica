package br.com.avaliacao.tarefas.repository;

import br.com.avaliacao.tarefas.model.StatusTarefa;
import br.com.avaliacao.tarefas.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    boolean existsByTituloIgnoreCaseAndStatusNot(String titulo, StatusTarefa status);

    boolean existsByTituloIgnoreCaseAndStatusNotAndIdNot(
            String titulo, StatusTarefa status, Long id);
}