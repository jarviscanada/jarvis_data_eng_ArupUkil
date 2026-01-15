package ca.jrvs.apps.trading;

import ca.jrvs.apps.trading.model.config.MarketDataConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"ca.jrvs.apps.trading.dao", "ca.jrvs.apps.trading.service"})
@EnableJpaRepositories(basePackages = "ca.jrvs.apps.trading.dao")
@EntityScan(basePackages = "ca.jrvs.apps.trading.model")
public class TestConfig {

  @Bean
  public DataSource dataSource() {
    Properties props = loadProperties();
    String dbUrl = getProperty(props, "db-url", "PSQL_TEST_URL", "PSQL_URL");
    String dbClass = getProperty(props, "db-class", "PSQL_TEST_DRIVER", "PSQL_DRIVER");
    String user = getProperty(props, "username", "PSQL_TEST_USER", "PSQL_USER");
    String password = getProperty(props, "password", "PSQL_TEST_PASSWORD", "PSQL_PASSWORD");

    if (dbUrl == null || dbUrl.trim().isEmpty()) {
      String server = getProperty(props, "server");
      String database = getProperty(props, "database");
      String port = getProperty(props, "port");
      if (server != null && database != null) {
        String portSegment = (port == null || port.trim().isEmpty()) ? "" : ":" + port;
        dbUrl = "jdbc:postgresql://" + server + portSegment + "/" + database;
      }
    }

    if (dbClass == null || dbClass.trim().isEmpty()) {
      dbClass = (dbUrl != null && dbUrl.startsWith("jdbc:h2:"))
          ? "org.h2.Driver"
          : "org.postgresql.Driver";
    }

    if (dbUrl == null || dbUrl.trim().isEmpty()) {
      throw new IllegalStateException("Missing database connection settings");
    }

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(dbClass);
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(user == null ? "" : user);
    dataSource.setPassword(password == null ? "" : password);
    return dataSource;
  }

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager() {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(50);
    cm.setDefaultMaxPerRoute(50);
    return cm;
  }

  @Bean
  public MarketDataConfig marketDataConfig() {
    Properties props = loadProperties();
    MarketDataConfig config = new MarketDataConfig();
    String host = getProperty(props, "api-host");
    String token = getProperty(props, "api-key");
    config.setHost(host == null || host.trim().isEmpty()
        ? "https://finnhub.io/api/v1/quote"
        : host);
    config.setToken(token == null ? "" : token);
    return config;
  }

  private Properties loadProperties() {
    Properties props = new Properties();
    loadFromClasspath(props, "test.properties");
    if (props.isEmpty()) {
      loadFromClasspath(props, "env.properties");
    }
    return props;
  }

  private void loadFromClasspath(Properties props, String resourceName) {
    ClassPathResource resource = new ClassPathResource(resourceName);
    if (!resource.exists()) {
      return;
    }
    try (InputStream in = resource.getInputStream()) {
      props.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load " + resourceName, e);
    }
  }

  private String getProperty(Properties props, String key, String... envKeys) {
    String value = props.getProperty(key);
    if (value != null && !value.trim().isEmpty()) {
      return value.trim();
    }
    for (String envKey : envKeys) {
      String envValue = System.getenv(envKey);
      if (envValue != null && !envValue.trim().isEmpty()) {
        return envValue.trim();
      }
    }
    return null;
  }
}
