package com.wesolve.platform.web;

import com.wesolve.platform.api.ApiException;
import io.micrometer.tracing.Tracer;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private final Tracer tracer;

  public ApiExceptionHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ProblemDetail> api(ApiException ex) {
    return ResponseEntity.status(ex.status())
        .body(problem(ex.status(), ex.code(), ex.getMessage(), List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
    List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    Map.of(
                        "field",
                        error.getField(),
                        "message",
                        String.valueOf(error.getDefaultMessage())))
            .toList();
    ProblemDetail body = problem(400, "VALIDATION_FAILED", "The request is invalid.", errors);
    return ResponseEntity.badRequest().body(body);
  }

  private ProblemDetail problem(
      int status, String code, String detail, List<Map<String, String>> errors) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), detail);
    body.setTitle(HttpStatus.valueOf(status).getReasonPhrase());
    body.setProperty("code", code);
    body.setProperty("traceId", traceId());
    if (!errors.isEmpty()) {
      body.setProperty("errors", errors);
    }
    return body;
  }

  private String traceId() {
    if (tracer.currentSpan() == null) {
      return "none";
    }
    return tracer.currentSpan().context().traceId();
  }
}
