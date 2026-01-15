package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.FinnhubQuote;
import ca.jrvs.apps.trading.model.config.MarketDataConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

@Repository
public class MarketDataDao {

  private static final String QUERY_PARAM_SYMBOL = "symbol";
  private static final String QUERY_PARAM_TOKEN = "token";
  private static final String ERROR_FIELD = "error";

  private Logger logger = LoggerFactory.getLogger(MarketDataDao.class);

  private HttpClientConnectionManager httpClientConnectionManager;
  private MarketDataConfig marketDataConfig;
  private ObjectMapper objectMapper;

  @Autowired
  public MarketDataDao(HttpClientConnectionManager httpClientConnectionManager,
      MarketDataConfig marketDataConfig) {
    this.httpClientConnectionManager = httpClientConnectionManager;
    this.marketDataConfig = marketDataConfig;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Get a FinnhubQuote
   *
   * @param ticker
   * @throws IllegalArgumentException if a given ticker is invalid
   * @throws DataRetrievalFailureException if HTTP request failed
   */
  public Optional<FinnhubQuote> findById(String ticker) {
    if (ticker == null || ticker.trim().isEmpty()) {
      throw new IllegalArgumentException("Ticker is required");
    }

    String url = UriComponentsBuilder.fromHttpUrl(marketDataConfig.getHost())
        .queryParam(QUERY_PARAM_SYMBOL, ticker)
        .queryParam(QUERY_PARAM_TOKEN, marketDataConfig.getToken())
        .build()
        .toUriString();

    logger.debug("Fetching Finnhub quote for {}", ticker);
    Optional<String> body = executeHttpGet(url);
    if (!body.isPresent()) {
      return Optional.empty();
    }

    FinnhubQuote quote = parseQuote(body.get(), ticker);
    if (quote == null || quote.getSymbol() == null || quote.getSymbol().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(quote);
  }

  /**
   * Get quotes from Finnhub
   *
   * @param tickers is a list of tickers
   * @return a list of FinnhubQuote objects
   * @throws IllegalArgumentException if a given ticker is invalid
   * @throws DataRetrievalFailureException if HTTP request failed
   */
  public List<FinnhubQuote> findAllById(Iterable<String> tickers) {
    if (tickers == null) {
      throw new IllegalArgumentException("Tickers are required");
    }

    List<FinnhubQuote> quotes = new ArrayList<>();
    for (String ticker : tickers) {
      Optional<FinnhubQuote> quote = findById(ticker);
      if (!quote.isPresent()) {
        throw new IllegalArgumentException("Invalid ticker: " + ticker);
      }
      quotes.add(quote.get());
    }
    return quotes;
  }

  /**
   * Execute a GET request and return http entity/body as a string
   * Tip: use EntitiyUtils.toString to process HTTP entity
   *
   * @param url resource URL
   * @return http response body or Optional.empty for 404 response
   * @throws DataRetrievalFailureException if HTTP failed or status code is unexpected
   */
  private Optional<String> executeHttpGet(String url) {
    HttpGet httpGet = new HttpGet(url);
    httpGet.setHeader("Accept", "application/json");

    try (CloseableHttpResponse response =
        (CloseableHttpResponse) getHttpClient().execute(httpGet)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_NOT_FOUND) {
        return Optional.empty();
      }
      if (statusCode != HttpStatus.SC_OK) {
        throw new DataRetrievalFailureException("Unexpected status code: " + statusCode);
      }
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        return Optional.empty();
      }
      return Optional.of(EntityUtils.toString(entity));
    } catch (IOException e) {
      throw new DataRetrievalFailureException("Failed to execute HTTP request", e);
    }
  }

  /**
   * Borrow a HTTP client from the HttpClientConnectionManager
   * @return a HttpClient
   */
  private HttpClient getHttpClient() {
    return HttpClients.custom()
        .setConnectionManager(httpClientConnectionManager)
        .setConnectionManagerShared(true)
        .build();
  }

  private FinnhubQuote parseQuote(String json, String ticker) {
    try {
      JsonNode root = objectMapper.readTree(json);
      if (root.has(ERROR_FIELD)) {
        throw new DataRetrievalFailureException(root.get(ERROR_FIELD).asText());
      }

      FinnhubQuote quote = objectMapper.treeToValue(root, FinnhubQuote.class);
      if (quote == null) {
        return null;
      }
      quote.setSymbol(ticker);
      if (quote.getCurrent() == null || quote.getCurrent().doubleValue() == 0d) {
        return null;
      }
      return quote;
    } catch (IOException e) {
      throw new DataRetrievalFailureException("Failed to parse response body", e);
    }
  }
}
