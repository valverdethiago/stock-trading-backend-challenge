package com.altruist.config;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration implements FlywayConfigurationCustomizer {

  @Value("${embedded-db.port}")
  int port;

  @SneakyThrows
  @Bean
  public DataSource dataSource() {
    EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.builder()
        .setPort(port)
        .start();
    return embeddedPostgres.getPostgresDatabase();
  }

  @Override
  public void customize(FluentConfiguration configuration) {
    configuration.dataSource(dataSource());
  }
}
