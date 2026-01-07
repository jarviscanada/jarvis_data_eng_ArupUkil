package ca.jrvs.apps.stockquote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PositionServiceUnitTest {
  @Mock
  private PositionDao positionDao;

  @Mock
  private QuoteDao quoteDao;

  @InjectMocks
  private PositionService positionService;

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
  void testBuySuccess() {
    when(quoteDao.findById("MSFT")).thenReturn(Optional.of(msft));
    when(positionDao.findById("MSFT")).thenReturn(Optional.empty());
    when(positionDao.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

    Position p = positionService.buy("msft", 10, 100);

    assertEquals("MSFT", p.getTicker());
    assertEquals(10, p.getNumOfShares());
    assertEquals(1000.0, p.getValuePaid(), 1e-9);

    verify(quoteDao).findById("MSFT");
    verify(positionDao).findById("MSFT");
    verify(positionDao).save(any(Position.class));
  }

  @Test
  void testBuyExceedsVolume() {
    when(quoteDao.findById("MSFT")).thenReturn(Optional.of(msft));
    assertThrows(IllegalArgumentException.class, () -> positionService.buy("MSFT", 1001, 100));
    verify(quoteDao).findById("MSFT");
    verifyNoInteractions(positionDao);
  }


  @Test
  void testSell() {
    positionService.sell("msft");
    verify(positionDao).deleteById("MSFT");
    assertThrows(IllegalArgumentException.class, () -> positionService.sell("   "));
  }

  @Test
  void testFindAll() {
    Position p1 = new Position();
    Position p2 = new Position();
    p1.setTicker("MSFT");
    p2.setTicker("AAPL");

    when(positionDao.findAll()).thenReturn(java.util.Arrays.asList(p1, p2));

    Iterable<Position> out = positionService.findAll();

    List<Position> list = new ArrayList<>();
    out.forEach(list::add);

    assertEquals(2, list.size());
    assertEquals("MSFT", list.get(0).getTicker());
    assertEquals("AAPL", list.get(1).getTicker());
    verify(positionDao).findAll();
  }
}
