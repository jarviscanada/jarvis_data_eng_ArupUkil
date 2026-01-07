package ca.jrvs.apps.stockquote.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntTestBase {
  @Container
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:9.6-alpine"))
          .withDatabaseName("stock_quote")
          .withUsername("postgres")
          .withPassword("password")
          .withInitScript("sql/ddl.sql"); // classpath path under src/test/resources

  protected Connection connection() throws Exception {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(),
        POSTGRES.getUsername(),
        POSTGRES.getPassword()
    );
  }

  @BeforeEach
  void clean() throws Exception {
    try (Connection c = connection(); Statement st = c.createStatement()) {
      st.executeUpdate("DELETE FROM position");
      st.executeUpdate("DELETE FROM quote");
    }
  }
}
