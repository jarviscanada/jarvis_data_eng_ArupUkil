package ca.jrvs.apps.stockquote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PositionServiceIntTest extends IntTestBase {
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

  private void insertQuote(QuoteDao quoteDao, String ticker, int volume, double price) {
    Quote q = new Quote();
    q.setTicker(ticker);
    q.setOpen(price);
    q.setHigh(price);
    q.setLow(price);
    q.setPrice(price);
    q.setVolume(volume);
    q.setLatestTradingDay(Date.valueOf("2023-10-13"));
    q.setPreviousClose(price);
    q.setChange(0);
    q.setChangePercent("0%");
    q.setTimestamp(new Timestamp(System.currentTimeMillis()));
    quoteDao.save(q);
  }

  @Test
  void testPositionServices() throws Exception {
    try (Connection c = connection()) {
      QuoteDao quoteDao = new QuoteDao(c);
      PositionDao positionDao = new PositionDao(c);
      PositionService positionService = new PositionService(positionDao, quoteDao);

      insertQuote(quoteDao, "MSFT", 200, 100);

      Position p = positionService.buy("msft", 10, 100);
      assertEquals(10, p.getNumOfShares());

      Optional<Position> fromDb = positionDao.findById("MSFT");
      assertTrue(fromDb.isPresent());
      assertEquals(10, fromDb.get().getNumOfShares());

      positionService.sell("MSFT");
      assertFalse(positionDao.findById("MSFT").isPresent());

      insertQuote(quoteDao, "AAPL", 200, 150);

      positionService.buy("MSFT", 10, 100);
      positionService.buy("AAPL", 5, 150);

      Iterable<Position> out = positionService.findAll();

      Map<String, Integer> sharesByTicker = new HashMap<>();
      for (Position pos : out) {
        sharesByTicker.put(pos.getTicker(), pos.getNumOfShares());
      }

      assertEquals(2, sharesByTicker.size());
      assertEquals(10, sharesByTicker.get("MSFT"));
      assertEquals(5, sharesByTicker.get("AAPL"));
    }
  }
}
