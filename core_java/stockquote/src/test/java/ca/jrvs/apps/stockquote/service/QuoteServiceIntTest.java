package ca.jrvs.apps.stockquote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceIntTest extends IntTestBase {
  @Mock
  QuoteHttpHelper httpHelper;
  private Quote msft;

  @BeforeEach
  void setup() {
    msft = new Quote();
    msft.setTicker("MSFT");
    msft.setOpen(100);
    msft.setHigh(110);
    msft.setLow(90);
    msft.setPrice(105);
    msft.setVolume(1000);
    msft.setLatestTradingDay(Date.valueOf("2023-10-13"));
    msft.setPreviousClose(101);
    msft.setChange(4);
    msft.setChangePercent("3.96%");
    msft.setTimestamp(new Timestamp(System.currentTimeMillis()));
  }

  @Test
  void testQuoteServices() throws Exception {
    try (Connection c = connection()) {
      QuoteDao quoteDao = new QuoteDao(c);
      QuoteService quoteService = new QuoteService(quoteDao, httpHelper);

      when(httpHelper.fetchQuoteInfo("MSFT")).thenReturn(msft);

      Optional<Quote> out = quoteService.fetchQuoteDataFromAPI("msft");
      assertTrue(out.isPresent());
      assertEquals("MSFT", out.get().getTicker());

      Optional<Quote> fromDb = quoteDao.findById("MSFT");
      assertTrue(fromDb.isPresent());
      assertEquals(105.0, fromDb.get().getPrice(), 1e-9);
    }
  }
}
