package com.wesolve.platform.web;

import com.wesolve.platform.api.ApiException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final String INVALID = "The request is invalid.";

  private final ProblemWriter problems;

  public ApiExceptionHandler(ProblemWriter problems) {
    this.problems = problems;
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ProblemDetail> api(ApiException ex) {
    return respond(ex.status(), ex.code(), ex.getMessage(), List.of());
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
    return respond(400, "VALIDATION_FAILED", INVALID, errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> constraint(ConstraintViolationException ex) {
    List<Map<String, String>> errors =
        ex.getConstraintViolations().stream()
            .map(
                violation -> {
                  String path = violation.getPropertyPath().toString();
                  String field = path.substring(path.lastIndexOf('.') + 1);
                  return Map.of("field", field, "message", violation.getMessage());
                })
            .toList();
    return respond(400, "VALIDATION_FAILED", INVALID, errors);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ProblemDetail> unreadable(Exception ex) {
    return respond(400, "VALIDATION_FAILED", INVALID, List.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> denied(AccessDeniedException ex) {
    return respond(403, "FORBIDDEN", "You do not hold the required action.", List.of());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ProblemDetail> missing(NoResourceFoundException ex) {
    return respond(404, "NOT_FOUND", "Not found.", List.of());
  }

  private ResponseEntity<ProblemDetail> respond(
      int status, String code, String detail, List<Map<String, String>> errors) {
    return ResponseEntity.status(status)
        .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
        .body(problems.build(status, code, detail, errors));
  }
}
