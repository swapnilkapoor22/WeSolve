package com.wesolve.platform.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicConfigController {

    private final String issuer;
    private final String clientId;

    public PublicConfigController(
            @Value("${app.public.issuer}") String issuer, @Value("${app.public.client-id}") String clientId) {
        this.issuer = issuer;
        this.clientId = clientId;
    }

    @GetMapping("/api/v1/public-config")
    public Map<String, String> publicConfig() {
        return Map.of("issuer", issuer, "clientId", clientId);
    }
}
