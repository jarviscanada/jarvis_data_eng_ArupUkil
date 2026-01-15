package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.MarketDataDao;
import ca.jrvs.apps.trading.dao.QuoteDao;
import ca.jrvs.apps.trading.exception.ResourceNotFoundException;
import ca.jrvs.apps.trading.model.FinnhubQuote;
import ca.jrvs.apps.trading.model.Quote;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

  private Logger logger = LoggerFactory.getLogger(QuoteService.class);
  private MarketDataDao marketDataDao;
  private QuoteDao quoteDao;

  @Autowired
  public QuoteService(MarketDataDao marketDataDao, QuoteDao quoteDao) {
    this.marketDataDao = marketDataDao;
    this.quoteDao = quoteDao;
  }

  /**
   * Update quote table against Finnhub source.
   *
   * @throws ResourceNotFoundException if ticker is not found from Finnhub
   * @throws IllegalArgumentException for invalid input
   */
  public void updateMarketData() {
    List<Quote> quotes = quoteDao.findAll();
    if (quotes.isEmpty()) {
      return;
    }

    List<Quote> updatedQuotes = new ArrayList<>();
    for (Quote quote : quotes) {
      FinnhubQuote fhQuote = findFinnhubQuoteByTicker(quote.getTicker());
      updatedQuotes.add(buildQuoteFromFinnhubQuote(fhQuote));
    }

    quoteDao.saveAll(updatedQuotes);
  }

  /**
   * Validate (against Finnhub) and save given tickers to quote table.
   *
   * @param tickers
   * @return list of converted quote entities
   * @throws IllegalArgumentException if ticker is not found from Finnhub
   */
  public List<Quote> saveQuotes(List<String> tickers) {
    if (tickers == null || tickers.isEmpty()) {
      throw new IllegalArgumentException("Tickers are required");
    }

    List<Quote> quotes = new ArrayList<>();
    for (String ticker : tickers) {
      quotes.add(saveQuote(ticker));
    }
    return quotes;
  }

  /**
   * Find a FinnhubQuote from the given ticker.
   *
   * @param ticker
   * @return corresponding FinnhubQuote object
   * @throws IllegalArgumentException if ticker is invalid
   */
  public FinnhubQuote findFinnhubQuoteByTicker(String ticker) {
    if (ticker == null || ticker.trim().isEmpty()) {
      throw new IllegalArgumentException("Ticker is required");
    }

    logger.debug("Finding Finnhub quote for {}", ticker);
    return marketDataDao.findById(ticker)
        .orElseThrow(() -> new ResourceNotFoundException("Ticker not found: " + ticker));
  }

  /**
   * Update a given quote to the quote table without validation.
   *
   * @param quote entity to save
   * @return the saved quote entity
   */
  public Quote saveQuote(Quote quote) {
    if (quote == null) {
      throw new IllegalArgumentException("Quote is required");
    }
    return quoteDao.save(quote);
  }

  /**
   * Find all quotes from the quote table.
   *
   * @return a list of quotes
   */
  public List<Quote> findAllQuotes() {
    return quoteDao.findAll();
  }

  /**
   * Helper method to map a FinnhubQuote to a Quote entity.
   */
  protected static Quote buildQuoteFromFinnhubQuote(FinnhubQuote fhQuote) {
    if (fhQuote == null || fhQuote.getSymbol() == null) {
      throw new IllegalArgumentException("Finnhub quote is invalid");
    }

    Quote quote = new Quote();
    quote.setTicker(fhQuote.getSymbol());

    Double price = valueOrZero(fhQuote.getCurrent());
    quote.setLastPrice(price);
    quote.setBidPrice(price);
    quote.setAskPrice(price);
    quote.setBidSize(0);
    quote.setAskSize(0);

    return quote;
  }

  /**
   * Helper method to validate and save a single ticker.
   */
  protected Quote saveQuote(String ticker) {
    FinnhubQuote fhQuote = findFinnhubQuoteByTicker(ticker);
    Quote quote = buildQuoteFromFinnhubQuote(fhQuote);
    return quoteDao.save(quote);
  }

  private static Double valueOrZero(Double value) {
    return value == null ? 0d : value;
  }
}
