package com.wesolve.platform;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wesolve.platform.security.RequiredSettingsEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;

class MissingSecretIT {

  @Test
  void namesTheMissingSettingAndNotItsValue() {
    StandardEnvironment environment = new StandardEnvironment();
    assertThatThrownBy(
            () ->
                new RequiredSettingsEnvironment()
                    .postProcessEnvironment(environment, new SpringApplication()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("POSTGRES_PASSWORD")
        .hasMessageNotContaining("super-secret");
  }
}
