package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.model.Quote;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionService {
  private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

  private PositionDao positionDao;
  private QuoteDao quoteDao;

  public PositionService(PositionDao positionDao, QuoteDao quoteDao) {
    this.positionDao = positionDao;
    this.quoteDao = quoteDao;
  }

  /**
   * Processes a buy order and updates the database accordingly
   * @param ticker
   * @param numberOfShares
   * @param price
   * @return The position in our database after processing the buy
   */
  public Position buy(String ticker, int numberOfShares, double price) {
    if (ticker == null || ticker.trim().isEmpty()) {
      logger.warn("buy called with invalid ticker={}", ticker);
      throw new IllegalArgumentException("Invalid ticker");
    }

    ticker = ticker.trim().toUpperCase();

    if (numberOfShares <= 0) {
      logger.warn("buy called with non-positive shares={} ticker={}", numberOfShares, ticker);
      throw new IllegalArgumentException("numberOfShares must be > 0");
    }

    if (price < 0.0) {
      logger.warn("buy called with non-positive price={} ticker={}", price, ticker);
      throw new IllegalArgumentException("price must be >= 0");
    }

    // Volume check uses the quote stored in DB
    Optional<Quote> quoteOpt = quoteDao.findById(ticker);
    if (!quoteOpt.isPresent()) {
      logger.warn("buy rejected: no quote in DB for ticker={}", ticker);
      throw new IllegalArgumentException(
          "No quote data found in DB for ticker=" + ticker + ". Fetch/save quote first.");
    }

    Quote q = quoteOpt.get();
    if (numberOfShares > q.getVolume()) {
      logger.warn("buy rejected: shares={} exceeds volume={} ticker={}",
          numberOfShares, q.getVolume(), ticker);
      throw new IllegalArgumentException("Cannot buy more than available volume");
    }

    logger.info("Processing buy ticker={} shares={} price={}", ticker, numberOfShares, price);

    Optional<Position> existingOpt = positionDao.findById(ticker);
    Position updated = new Position();
    updated.setTicker(ticker);

    if (existingOpt.isPresent()) {
      Position existing = existingOpt.get();
      int newShares = existing.getNumOfShares() + numberOfShares;
      double newValuePaid = existing.getValuePaid() + (numberOfShares * price);

      updated.setNumOfShares(newShares);
      updated.setValuePaid(newValuePaid);

      logger.info("Updated position ticker={} oldShares={} newShares={}",
          ticker, existing.getNumOfShares(), newShares);
    }
    else {
      updated.setNumOfShares(numberOfShares);
      updated.setValuePaid(numberOfShares * price);

      logger.info("Created new position ticker={} shares={}", ticker, numberOfShares);
    }

    Position saved = positionDao.save(updated);
    logger.info("Position saved ticker={} shares={} valuePaid={}",
        saved.getTicker(), saved.getNumOfShares(), saved.getValuePaid());

    return saved;
  }

  /**
   * Sells all shares of the given ticker symbol
   * @param ticker
   */
  public void sell(String ticker) {
    if (ticker == null || ticker.trim().isEmpty()) {
      logger.warn("sell called with invalid ticker={}", ticker);
      throw new IllegalArgumentException("Invalid ticker");
    }

    ticker = ticker.trim().toUpperCase();
    logger.info("Selling (deleting) position ticker={}", ticker);
    positionDao.deleteById(ticker);
  }

  public Iterable<Position> findAll() {
    logger.info("Fetching all positions");

    Iterable<Position> positions = positionDao.findAll();

    List<Position> out = new ArrayList<>();
    for (Position p : positions) {
      out.add(p);
    }

    logger.debug("Fetched positions count={}", out.size());
    return out;
  }
}
