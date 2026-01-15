package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.FinnhubQuote;
import ca.jrvs.apps.trading.model.config.MarketDataConfig;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
public class MarketDataDaoIntTest {

  private static final String RESPONSE_BODY =
      "{\"c\":123.45,\"h\":124.0,\"l\":120.0,\"o\":121.0,\"pc\":119.0,\"t\":1710000000}";

  private static HttpServer server;
  private static int port;

  @Autowired
  private MarketDataDao marketDataDao;

  @Autowired
  private MarketDataConfig marketDataConfig;

  @BeforeClass
  public static void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/quote", exchange -> {
      byte[] body = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.start();
    port = server.getAddress().getPort();
  }

  @AfterClass
  public static void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Before
  public void setup() {
    marketDataConfig.setHost("http://localhost:" + port + "/quote");
    marketDataConfig.setToken("test");
  }

  @Test
  public void findById() {
    Optional<FinnhubQuote> quote = marketDataDao.findById("IBM");
    assertTrue(quote.isPresent());
    assertEquals("IBM", quote.get().getSymbol().toUpperCase());
  }

  @Test
  public void findAllById() {
    List<FinnhubQuote> quotes = marketDataDao.findAllById(Arrays.asList("IBM"));
    assertEquals(1, quotes.size());
    assertEquals("IBM", quotes.get(0).getSymbol().toUpperCase());
  }
}
