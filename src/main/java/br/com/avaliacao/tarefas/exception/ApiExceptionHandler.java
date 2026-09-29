package br.com.avaliacao.tarefas.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception, HttpServletRequest request) {
        return criarErro(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiError> tratarRegraDeNegocio(
            RegraDeNegocioException exception, HttpServletRequest request) {
        return criarErro(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("; "));
        return criarErro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarJsonInvalido(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return criarErro(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou status desconhecido", request);
    }

    private ResponseEntity<ApiError> criarErro(
            HttpStatus status, String mensagem, HttpServletRequest request) {
        ApiError erro = new ApiError(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem,
                request.getRequestURI());
        return ResponseEntity.status(status).body(erro);
    }

    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}