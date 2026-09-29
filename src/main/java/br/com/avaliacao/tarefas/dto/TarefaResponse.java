package br.com.avaliacao.tarefas.dto;

import br.com.avaliacao.tarefas.model.StatusTarefa;
import java.time.LocalDateTime;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao) {
}