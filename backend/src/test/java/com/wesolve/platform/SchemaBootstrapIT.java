package com.wesolve.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.wesolve.support.PostgresIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class SchemaBootstrapIT extends PostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Flyway flyway;

    @Test
    void secondMigrateDoesNotDuplicateSeed() {
        assertThat(count("access_types")).isEqualTo(9);
        assertThat(count("groups")).isEqualTo(3);
        assertThat(count("roles")).isEqualTo(3);
        flyway.migrate();
        assertThat(count("access_types")).isEqualTo(9);
        assertThat(count("groups")).isEqualTo(3);
    }

    private int count(String table) {
        Integer value = jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return value == null ? 0 : value;
    }
}
