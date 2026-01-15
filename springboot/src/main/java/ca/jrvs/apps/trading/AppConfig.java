package ca.jrvs.apps.trading;

import ca.jrvs.apps.trading.model.config.MarketDataConfig;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ca.jrvs.apps.trading.dao")
@EntityScan(basePackages = "ca.jrvs.apps.trading.model")
@PropertySource("classpath:env.properties")
public class AppConfig {

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager() {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(50);
    cm.setDefaultMaxPerRoute(50);
    return cm;
  }

  @Bean
  public MarketDataConfig marketDataConfig(
      @Value("${marketdata.host:${api-host:${MARKETDATA_HOST:https://finnhub.io/api/v1/quote}}}")
          String host,
      @Value("${marketdata.token:${api-key:${FINNHUB_TOKEN:${IEX_PUB_TOKEN:}}}}")
          String token) {
    MarketDataConfig config = new MarketDataConfig();
    config.setHost(host);
    config.setToken(token);
    return config;
  }

  @Bean
  public DataSource dataSource(
      @Value("${db-url:${DB_URL:${PSQL_URL:}}}") String dbUrl,
      @Value("${db-class:${DB_CLASS:${PSQL_DRIVER:org.postgresql.Driver}}}") String dbClass,
      @Value("${server:${DB_HOST:${PSQL_HOST:}}}") String server,
      @Value("${port:${DB_PORT:${PSQL_PORT:}}}") String port,
      @Value("${database:${DB_NAME:${PSQL_DB:}}}") String database,
      @Value("${username:${DB_USER:${PSQL_USER:}}}") String username,
      @Value("${password:${DB_PASSWORD:${PSQL_PASSWORD:}}}") String password) {
    String url = dbUrl;
    if (url == null || url.trim().isEmpty()) {
      if (server != null && !server.trim().isEmpty()
          && database != null && !database.trim().isEmpty()) {
        String portSegment = (port == null || port.trim().isEmpty()) ? "" : ":" + port.trim();
        url = "jdbc:postgresql://" + server.trim() + portSegment + "/" + database.trim();
      }
    }
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalStateException("Missing database connection settings");
    }

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(dbClass);
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    return dataSource;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setDataSource(dataSource);
    factory.setPackagesToScan("ca.jrvs.apps.trading.model");
    JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    factory.setJpaVendorAdapter(adapter);

    Properties jpaProps = new Properties();
    jpaProps.setProperty("hibernate.hbm2ddl.auto", "validate");
    factory.setJpaProperties(jpaProps);
    return factory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
