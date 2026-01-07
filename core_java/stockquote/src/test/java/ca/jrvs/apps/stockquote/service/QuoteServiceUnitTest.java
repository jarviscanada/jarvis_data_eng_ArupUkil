package ca.jrvs.apps.stockquote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceUnitTest {
  @Mock
  private QuoteDao quoteDao;

  @Mock
  private QuoteHttpHelper httpHelper;

  @InjectMocks
  private QuoteService quoteService;

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
  void testFetchQuoteDataFromAPISuccess() {
    when(httpHelper.fetchQuoteInfo("MSFT")).thenReturn(msft);
    when(quoteDao.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

    Optional<Quote> out = quoteService.fetchQuoteDataFromAPI("msft");
    assertTrue(out.isPresent());
    assertEquals("MSFT", out.get().getTicker());

    verify(httpHelper).fetchQuoteInfo("MSFT");
    verify(quoteDao).save(any(Quote.class));
  }

  @Test
  void testFetchQuoteDataFromAPINotFound() {
    when(httpHelper.fetchQuoteInfo("MSFT")).thenThrow(new IllegalArgumentException("No quote data for symbol=MSFT"));
    Optional<Quote> out = quoteService.fetchQuoteDataFromAPI("msft");
    assertFalse(out.isPresent());
    verify(httpHelper).fetchQuoteInfo("MSFT");
    verifyNoInteractions(quoteDao);
  }

  @Test
  void testFetchQuoteDataFromAPIEmpty() {
    Optional<Quote> out = quoteService.fetchQuoteDataFromAPI("   ");
    assertFalse(out.isPresent());
    verifyNoInteractions(httpHelper);
    verifyNoInteractions(quoteDao);
  }
}
