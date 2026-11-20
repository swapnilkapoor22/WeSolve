package com.wesolve.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.wesolve.WeSolveApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

class MissingSecretIT {

    @Test
    void namesTheMissingSettingAndNotItsValue() {
        SpringApplication application = new SpringApplication(WeSolveApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(
                () -> application.run("--spring.main.banner-mode=off", "--POSTGRES_PASSWORD="));
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage() + " " + causeText(thrown)).contains("POSTGRES_PASSWORD");
        assertThat(thrown.getMessage() + " " + causeText(thrown)).doesNotContain("super-secret");
    }

    private static String causeText(Throwable thrown) {
        StringBuilder text = new StringBuilder();
        Throwable current = thrown;
        while (current != null) {
            text.append(' ').append(current.getMessage());
            current = current.getCause();
        }
        return text.toString();
    }
}
