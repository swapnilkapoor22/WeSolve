package com.wesolve.platform;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wesolve.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** Web surface shared by every story: error bodies, SPA forwarding, and public settings. */
class WebSurfaceIT extends PostgresIntegrationTest {

  @Autowired private MockMvc mvc;

  @Test
  void unauthenticatedApiCallGetsProblemWithUnauthenticatedCode() throws Exception {
    mvc.perform(get("/api/v1/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("Content-Type", "application/problem+json"))
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void unauthenticatedWriteGetsProblemAndCreatesNothing() throws Exception {
    mvc.perform(post("/api/v1/tickets").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
  }

  @Test
  void unknownPersonGetsNoAccessProblem() throws Exception {
    mvc.perform(
            get("/api/v1/me")
                .with(jwt().jwt(j -> j.claim("preferred_username", "nobody@example.com"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.code").value("NO_ACCESS"))
        .andExpect(jsonPath("$.traceId").exists());
  }

  @Test
  void rootAndDeepLinksForwardToTheSpaShell() throws Exception {
    mvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
    mvc.perform(get("/tickets/TKT-1")).andExpect(forwardedUrl("/index.html"));
  }

  @Test
  void apiPathsAreNotForwardedToTheShell() throws Exception {
    mvc.perform(get("/api/v1/unknown")).andExpect(status().isUnauthorized());
  }

  @Test
  void publicConfigIsOpenAndReturnsIssuerAndClient() throws Exception {
    mvc.perform(get("/api/v1/public-config"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.issuer").value("http://localhost:8082/realms/wesolve"))
        .andExpect(jsonPath("$.clientId").value("wesolve"));
  }
}
