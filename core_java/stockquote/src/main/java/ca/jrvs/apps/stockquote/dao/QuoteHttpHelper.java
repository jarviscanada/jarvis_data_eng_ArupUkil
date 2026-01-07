package ca.jrvs.apps.stockquote.dao;
import ca.jrvs.apps.stockquote.dto.AlphaVantageQuoteMixin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import okhttp3.OkHttpClient;
import ca.jrvs.apps.stockquote.model.Quote;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteHttpHelper {
  private String apiKey;
  private OkHttpClient client;
  private ObjectMapper mapper;

  private static String HOST = "alpha-vantage.p.rapidapi.com";
  private static String URL_TEMPLATE =
      "https://alpha-vantage.p.rapidapi.com/query?function=GLOBAL_QUOTE&symbol=%s&datatype=json";

  private static final Logger logger = LoggerFactory.getLogger(QuoteHttpHelper.class);

  public QuoteHttpHelper(String apiKey, OkHttpClient client) {
    this.apiKey = apiKey;
    this.client = client;
    this.mapper = new ObjectMapper();
    // Alpha Vantage API calls so using that mixin and connecting it to Quote model
    mapper.addMixIn(Quote.class, AlphaVantageQuoteMixin.class);
  }

  /**
   * Fetch latest quote data from Alpha Vantage endpoint
   * @param symbol
   * @return Quote with latest data
   * @throws IllegalArgumentException - if no data was found for the given symbol
   */
  public Quote fetchQuoteInfo(String symbol) throws IllegalArgumentException {
    if (symbol == null || symbol.trim().isEmpty()) {
      logger.warn("fetchQuoteInfo called with blank symbol");
      throw new IllegalArgumentException("Symbol is blank");
    }


    String ticker = symbol.trim().toUpperCase();
    logger.info("Fetching quote from Alpha Vantage for symbol={}", ticker);

    Request request = new Request.Builder()
        .url(String.format(URL_TEMPLATE, ticker))
        .addHeader("X-RapidAPI-Key", apiKey)
        .addHeader("X-RapidAPI-Host", HOST)
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        int code = response.code();
        logger.error("Alpha Vantage call failed for symbol={} httpCode={}", ticker, code);
        throw new IllegalArgumentException("HTTP failure: " + code);
      }

      String json = response.body().string();
      logger.debug("Raw Alpha Vantage response for symbol={} length={}", ticker, json.length());

      JsonNode root = mapper.readTree(json);
      JsonNode quoteNode = root.path("Global Quote");

      if (quoteNode == null || quoteNode.isMissingNode() || quoteNode.isEmpty()) {
        throw new IllegalArgumentException("No quote data for symbol=" + ticker);
      }

      Quote q = mapper.treeToValue(quoteNode, Quote.class);

      q.setTimestamp(new Timestamp(System.currentTimeMillis()));

      logger.info("Fetched quote OK symbol={} price={} volume={}",
          q.getTicker(), q.getPrice(), q.getVolume());

      return q;

    } catch (IOException e) {
      logger.error("Failed to fetch/parse quote for symbol={}", ticker, e);
      throw new IllegalArgumentException("Failed to fetch/parse quote for " + ticker, e);
    } catch (RuntimeException e) {
      // catches NumberFormatException, Date parse issues, etc.
      logger.error("Unexpected parsing/runtime error for symbol={}", ticker, e);
      throw e;
    }
  }
}
