package com.wesolve.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/** Builds RFC 9457 bodies with the stable {@code code} and {@code traceId} the contract needs. */
@Component
public class ProblemWriter {

  private final Tracer tracer;
  private final ObjectMapper mapper;

  public ProblemWriter(Tracer tracer, ObjectMapper mapper) {
    this.tracer = tracer;
    this.mapper = mapper;
  }

  public ProblemDetail build(
      int status, String code, String detail, List<Map<String, String>> errors) {
    HttpStatus http = HttpStatus.valueOf(status);
    ProblemDetail body = ProblemDetail.forStatusAndDetail(http, detail);
    body.setTitle(http.getReasonPhrase());
    body.setProperty("code", code);
    body.setProperty("traceId", traceId());
    if (!errors.isEmpty()) {
      body.setProperty("errors", errors);
    }
    return body;
  }

  public void write(HttpServletResponse response, int status, String code, String detail)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), build(status, code, detail, List.of()));
  }

  private String traceId() {
    var span = tracer.currentSpan();
    return span == null ? "none" : span.context().traceId();
  }
}
