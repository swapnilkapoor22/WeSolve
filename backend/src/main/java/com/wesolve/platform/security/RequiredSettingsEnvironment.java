package com.wesolve.platform.security;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class RequiredSettingsEnvironment implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        String password = environment.getProperty("POSTGRES_PASSWORD");
        String datasourcePassword = environment.getProperty("spring.datasource.password");
        if (isBlank(password) && isBlank(datasourcePassword)) {
            throw new IllegalStateException("Missing required setting: POSTGRES_PASSWORD");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
