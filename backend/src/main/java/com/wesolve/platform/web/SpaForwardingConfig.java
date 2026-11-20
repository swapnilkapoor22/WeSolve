package com.wesolve.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SpaForwardingConfig extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String uri = request.getRequestURI();
    boolean file = uri.contains(".");
    boolean api = uri.startsWith("/api/") || uri.startsWith("/actuator");
    if ("GET".equals(request.getMethod()) && !file && !api) {
      request.getRequestDispatcher("/index.html").forward(request, response);
      return;
    }
    chain.doFilter(request, response);
  }
}
