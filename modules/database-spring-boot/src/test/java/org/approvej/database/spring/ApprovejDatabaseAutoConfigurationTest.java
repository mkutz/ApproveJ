package org.approvej.database.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.approvej.database.jdbc.RecordingDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ApprovejDatabaseAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ApprovejDatabaseAutoConfiguration.class))
          .withUserConfiguration(DataSourceConfiguration.class);

  @Test
  void recordingDataSourceBeanPostProcessor() {
    contextRunner.run(
        context -> {
          DataSource dataSource = context.getBean(DataSource.class);
          assertThat(dataSource).isInstanceOf(RecordingDataSource.class);

          try (Connection connection = dataSource.getConnection();
              Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
          }

          assertThat(((RecordingDataSource) dataSource).recordedQueries())
              .containsExactly("SELECT 1");
        });
  }

  @Test
  void recordingDataSourceBeanPostProcessor_disabled() {
    contextRunner
        .withPropertyValues("approvej.database.recording.enabled=false")
        .run(
            context ->
                assertThat(context.getBean(DataSource.class))
                    .isNotInstanceOf(RecordingDataSource.class));
  }

  @Configuration(proxyBeanMethods = false)
  static class DataSourceConfiguration {

    @Bean
    DataSource dataSource() {
      JdbcDataSource dataSource = new JdbcDataSource();
      dataSource.setUrl("jdbc:h2:mem:approvej-spring;DB_CLOSE_DELAY=-1");
      return dataSource;
    }
  }
}
