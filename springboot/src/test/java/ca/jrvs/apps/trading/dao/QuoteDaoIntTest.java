package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.Quote;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class QuoteDaoIntTest {

  @Autowired
  private QuoteDao quoteDao;

  private Quote savedQuote;

  @Before
  public void setup() {
    quoteDao.deleteAll();
    savedQuote = buildQuote("AAPL");
    quoteDao.save(savedQuote);
  }

  @After
  public void tearDown() {
    quoteDao.deleteAll();
  }

  @Test
  public void save() {
    Quote quote = buildQuote("MSFT");
    quoteDao.save(quote);
    assertTrue(quoteDao.existsById("MSFT"));
  }

  @Test
  public void saveAll() {
    Quote quote = buildQuote("MSFT");
    quoteDao.saveAll(Arrays.asList(savedQuote, quote));
    assertTrue(quoteDao.existsById("MSFT"));
  }

  @Test
  public void findAll() {
    List<Quote> quotes = quoteDao.findAll();
    assertTrue(quotes.size() >= 1);
  }

  @Test
  public void findById() {
    Quote quote = quoteDao.findById("AAPL").orElse(null);
    assertTrue(quote != null && "AAPL".equals(quote.getTicker()));
  }

  @Test
  public void existsById() {
    assertTrue(quoteDao.existsById("AAPL"));
  }

  @Test
  public void deleteById() {
    quoteDao.deleteById("AAPL");
    assertFalse(quoteDao.existsById("AAPL"));
  }

  @Test
  public void count() {
    assertTrue(quoteDao.count() >= 1);
  }

  @Test
  public void deleteAll() {
    quoteDao.deleteAll();
    assertEquals(0, quoteDao.count());
  }

  private Quote buildQuote(String ticker) {
    Quote quote = new Quote();
    quote.setTicker(ticker);
    quote.setAskPrice(10d);
    quote.setAskSize(10);
    quote.setBidPrice(10.2d);
    quote.setBidSize(12);
    quote.setLastPrice(10.1d);
    return quote;
  }
}
