package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteDao implements CrudDao<Quote, String> {
  private static final Logger logger = LoggerFactory.getLogger(QuoteDao.class);
  private final Connection c;

  public QuoteDao(Connection c) {
    this.c = c;
  }

  /**
   * Helper method that turns ResultSet to Quote objects
   * @param rs ResultSet with info for a Quote object
   * @return Created Quote object
   * @throws SQLException
   */
  private Quote resultsToQuote(ResultSet rs) throws SQLException {
    Quote q = new Quote();
    q.setTicker(rs.getString("symbol"));
    q.setOpen(rs.getDouble("open"));
    q.setHigh(rs.getDouble("high"));
    q.setLow(rs.getDouble("low"));
    q.setPrice(rs.getDouble("price"));
    q.setVolume(rs.getInt("volume"));
    q.setLatestTradingDay(rs.getDate("latest_trading_day"));
    q.setPreviousClose(rs.getDouble("previous_close"));
    q.setChange(rs.getDouble("change"));
    q.setChangePercent(rs.getString("change_percent"));
    q.setTimestamp(rs.getTimestamp("timestamp"));
    return q;
  }

  @Override
  public Quote save(Quote q) throws IllegalArgumentException {
    if (q == null || q.getTicker() == null || q.getTicker().trim().isEmpty()) {
      logger.warn("QuoteDao.save called with null quote or blank ticker");
      throw new IllegalArgumentException("Quote/ticker is null or blank");
    }

    logger.info("Saving quote to DB symbol={}", q.getTicker());

    // Creates a new row unless already exists which then updates instead
    // Postgres EXCLUDED allows to reduce parameter count as it refers to values
    // that have been attempted to be inserted
    String sql =
      "INSERT INTO quote(symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
      "ON CONFLICT (symbol) DO UPDATE SET " +
      "open = EXCLUDED.open, high = EXCLUDED.high, low = EXCLUDED.low, price = EXCLUDED.price, " +
      "volume = EXCLUDED.volume, latest_trading_day = EXCLUDED.latest_trading_day, " +
      "previous_close = EXCLUDED.previous_close, change = EXCLUDED.change, change_percent = EXCLUDED.change_percent, " +
      "timestamp = CURRENT_TIMESTAMP";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, q.getTicker());
      ps.setDouble(2, q.getOpen());
      ps.setDouble(3, q.getHigh());
      ps.setDouble(4, q.getLow());
      ps.setDouble(5, q.getPrice());
      ps.setInt(6, q.getVolume());
      ps.setDate(7, q.getLatestTradingDay());
      ps.setDouble(8, q.getPreviousClose());
      ps.setDouble(9, q.getChange());
      ps.setString(10, q.getChangePercent());
      int rows = ps.executeUpdate();

      logger.debug("QuoteDao.save affectedRows={} symbol={}", rows, q.getTicker());
      return q;

    } catch (SQLException e) {
      logger.error("DB error saving quote symbol={}", q.getTicker(), e);
      throw new RuntimeException("Failed to save quote " + q.getTicker(), e);
    }
  }

  @Override
  public Optional<Quote> findById(String s) throws IllegalArgumentException {
    if (s == null || s.trim().isEmpty()) {
      logger.warn("QuoteDao.findById called with null/blank symbol");
      throw new IllegalArgumentException("Symbol is null/blank");
    }
    String ticker = s.trim().toUpperCase();
    logger.info("Fetching quote from DB symbol={}", ticker);

    String sql = 
    "SELECT symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent, timestamp " +
    "FROM quote WHERE symbol=?";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ticker);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        logger.info("No quote found in DB symbol={}", ticker);
        return Optional.empty();
      }

      Quote q = resultsToQuote(rs);
      logger.debug("Found quote in DB symbol={} price={}", q.getTicker(), q.getPrice());
      return Optional.of(q);
    } catch (SQLException e) {
      logger.error("DB error finding quote symbol={}", ticker, e);
      throw new RuntimeException("Failed to find quote " + ticker, e);
    }
  }

  @Override
  public Iterable<Quote> findAll() {
    logger.info("Finding all quotes");

    String sql =
      "SELECT symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent, timestamp " +
      "FROM quote";

    List<Quote> quotes = new ArrayList<>();

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        quotes.add(resultsToQuote(rs));
      }
      logger.debug("QuoteDao.findAll returnedCount={}", quotes.size());
      return quotes;

    } catch (SQLException e) {
      logger.error("DB error finding all quotes", e);
      throw new RuntimeException("Unable to find all Quotes", e);
    }
  }

  @Override
  public void deleteById(String s) throws IllegalArgumentException {
    if (s == null || s.trim().isEmpty()) {
      logger.warn("QuoteDao.deleteById called with null/blank symbol");
      throw new IllegalArgumentException("Symbol is null/blank");
    }

    String ticker = s.trim().toUpperCase();
    logger.info("Deleting quote by DB symbol={}", ticker);

    String sql = "DELETE FROM quote WHERE symbol = ?";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ticker);
      int rows = ps.executeUpdate();
      logger.debug("QuoteDao.deleteById affectedRows={} ticker={}", rows, ticker);
    } catch (SQLException e) {
      logger.error("DB error deleting quote ticker={}", ticker, e);
      throw new RuntimeException("Unable to delete Quote: " + ticker, e);
    }
  }

  @Override
  public void deleteAll() {
    logger.info("Deleting all quotes");

    // This may fail if position table still has rows referencing quote.symbol (FK constraint).
    String sql = "DELETE FROM quote";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      int rows = ps.executeUpdate();
      logger.debug("QuoteDao.deleteAll affectedRows={}", rows);
    } catch (SQLException e) {
      logger.error("DB error deleting all quotes (FK from position might be blocking)", e);
      throw new RuntimeException("Unable to delete all Quotes", e);
    }
  }
}
