package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Quote;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteService {
  private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

  private QuoteDao dao;
  private QuoteHttpHelper httpHelper;

  public QuoteService(QuoteDao dao, QuoteHttpHelper httpHelper) {
    this.dao = dao;
    this.httpHelper = httpHelper;
  }

  /**
   * Fetches latest quote data from endpoint
   * @param ticker
   * @return Latest quote information or empty optional if ticker symbol not found
   */
  public Optional<Quote> fetchQuoteDataFromAPI(String ticker) {
    if (ticker == null || ticker.trim().isEmpty()) {
      logger.warn("fetchQuoteDataFromAPI called with invalid ticker={}", ticker);
      return Optional.empty();
    }
    ticker = ticker.trim().toUpperCase();

    try {
      Quote q = httpHelper.fetchQuoteInfo(ticker);
      Quote saved = dao.save(q);
      logger.info("Fetched quote from API symbol={} price={} volume={}",
          saved.getTicker(), saved.getPrice(), saved.getVolume());
      return Optional.of(saved);
    } catch (IllegalArgumentException e) {
      logger.warn("API returned no/invalid data for symbol={} reason={}", ticker, e.getMessage());
      return Optional.empty();
    }
  }
}
