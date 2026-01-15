package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.dao.MarketDataDao;
import ca.jrvs.apps.trading.dao.QuoteDao;
import ca.jrvs.apps.trading.model.FinnhubQuote;
import ca.jrvs.apps.trading.model.Quote;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class QuoteServiceIntTest {

  @Autowired
  private QuoteService quoteService;

  @Autowired
  private QuoteDao quoteDao;

  @MockBean
  private MarketDataDao marketDataDao;

  @Before
  public void setup() {
    quoteDao.deleteAll();
    when(marketDataDao.findById(anyString())).thenAnswer(invocation -> {
      String ticker = invocation.getArgument(0);
      FinnhubQuote quote = new FinnhubQuote();
      quote.setSymbol(ticker);
      quote.setCurrent(100.0);
      return java.util.Optional.of(quote);
    });
  }

  @Test
  public void findFinnhubQuoteByTicker() {
    FinnhubQuote quote = quoteService.findFinnhubQuoteByTicker("IBM");
    assertNotNull(quote);
    assertEquals("IBM", quote.getSymbol().toUpperCase());
  }

  @Test
  public void updateMarketData() {
    Quote quote = new Quote();
    quote.setTicker("IBM");
    quote.setLastPrice(1d);
    quoteDao.save(quote);

    quoteService.updateMarketData();
    Quote updated = quoteDao.findById("IBM").orElse(null);
    assertNotNull(updated);
    assertNotNull(updated.getLastPrice());
  }

  @Test
  public void saveQuotes() {
    List<Quote> quotes = quoteService.saveQuotes(Arrays.asList("IBM"));
    assertEquals(1, quotes.size());
    assertTrue(quoteDao.existsById("IBM"));
  }

  @Test
  public void saveQuote() {
    Quote quote = new Quote();
    quote.setTicker("AAPL");
    quote.setLastPrice(1d);
    Quote saved = quoteService.saveQuote(quote);
    assertNotNull(saved);
    assertTrue(quoteDao.existsById("AAPL"));
  }

  @Test
  public void findAllQuotes() {
    Quote quote = new Quote();
    quote.setTicker("AAPL");
    quote.setLastPrice(1d);
    quoteDao.save(quote);

    List<Quote> quotes = quoteService.findAllQuotes();
    assertTrue(quotes.size() >= 1);
  }
}
