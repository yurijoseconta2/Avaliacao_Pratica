package br.com.avaliacao.tarefas.controller;

import br.com.avaliacao.tarefas.dto.TarefaRequest;
import br.com.avaliacao.tarefas.dto.TarefaResponse;
import br.com.avaliacao.tarefas.service.TarefaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest request) {
        TarefaResponse tarefa = tarefaService.criar(request);
        return ResponseEntity.created(URI.create("/api/tarefas/" + tarefa.id())).body(tarefa);
    }

    @GetMapping
    public ResponseEntity<List<TarefaResponse>> listar() {
        return ResponseEntity.ok(tarefaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody TarefaRequest request) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}