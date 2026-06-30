package org.approvej.database.spring;

import javax.sql.DataSource;
import org.approvej.database.jdbc.RecordingDataSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that wraps every Spring-managed {@link DataSource} with a {@link
 * RecordingDataSource}, so the SQL your code sends can be approved without any manual wiring.
 *
 * <p>Add the {@code database-spring-boot} module to your test dependencies and the {@link
 * DataSource} injected into your services and repositories becomes a {@link RecordingDataSource}.
 * Inject it and cast it to access the recorded queries:
 *
 * <pre>{@code
 * @Autowired DataSource dataSource;
 *
 * RecordingDataSource recordingDataSource = (RecordingDataSource) dataSource;
 * }</pre>
 *
 * <p>Recording is enabled by default. Set {@code approvej.database.recording.enabled=false} to
 * disable it, for example in contexts where the plain {@link DataSource} is required.
 */
@NullMarked
@AutoConfiguration
@ConditionalOnClass({DataSource.class, RecordingDataSource.class})
@ConditionalOnProperty(
    name = "approvej.database.recording.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ApprovejDatabaseAutoConfiguration {

  /** Creates a new {@link ApprovejDatabaseAutoConfiguration}. */
  public ApprovejDatabaseAutoConfiguration() {}

  /**
   * A {@link BeanPostProcessor} that wraps each {@link DataSource} bean with a {@link
   * RecordingDataSource}, unless it already is one.
   *
   * <p>The bean is {@code static} so it is instantiated early enough to post-process the {@link
   * DataSource} without prematurely initializing other beans.
   *
   * @return the {@link BeanPostProcessor} wrapping {@link DataSource} beans
   */
  @Bean
  static BeanPostProcessor recordingDataSourceBeanPostProcessor() {
    return new BeanPostProcessor() {
      @Override
      public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource && !(bean instanceof RecordingDataSource)) {
          return new RecordingDataSource(dataSource);
        }
        return bean;
      }
    };
  }
}
