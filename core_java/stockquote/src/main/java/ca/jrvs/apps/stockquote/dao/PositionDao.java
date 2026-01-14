package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Position;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionDao implements CrudDao<Position, String> {
  private static final Logger logger = LoggerFactory.getLogger(PositionDao.class);

  private final Connection c;

  public PositionDao(Connection c) {
    this.c = c;
  }

  /**
   * Helper method that turns ResultSet to Position objects
   * @param rs ResultSet with info for a Position object
   * @return Created Position object
   * @throws SQLException
   */
  private Position resultsToPosition(ResultSet rs) throws SQLException {
    Position p = new Position();
    p.setTicker(rs.getString("symbol"));
    p.setNumOfShares(rs.getInt("number_of_shares"));
    p.setValuePaid(rs.getDouble("value_paid"));
    return p;
  }

  @Override
  public Position save(Position p) throws IllegalArgumentException {
    if (p == null || p.getTicker() == null || p.getTicker().trim().isEmpty()) {
      logger.warn("PositionDao.save called with null position or blank ticker");
      throw new IllegalArgumentException("Position/ticker is null or blank");
    }

    String ticker = p.getTicker();
    logger.info("Saving position ticker={} shares={} valuePaid={}",
        ticker, p.getNumOfShares(), p.getValuePaid());

    // Creates a new row unless already exists which then updates instead
    // Postgres EXCLUDED allows to reduce parameter count as it refers to values
    // that have been attempted to be inserted
    String sql =
        "INSERT INTO position(symbol, number_of_shares, value_paid) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (symbol) DO UPDATE SET " +
            "number_of_shares = EXCLUDED.number_of_shares, " +
            "value_paid = EXCLUDED.value_paid";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ticker);
      ps.setInt(2, p.getNumOfShares());
      ps.setDouble(3, p.getValuePaid());

      int rows = ps.executeUpdate();
      logger.debug("PositionDao.save affectedRows={} ticker={}", rows, ticker);

      p.setTicker(ticker);
      return p;

    } catch (SQLException e) {
      // FK violation if quote row doesn't exist for this symbol
      logger.error("DB error saving position ticker={}", ticker, e);
      throw new RuntimeException("Unable to save Position: " + ticker, e);
    }
  }

  @Override
  public Optional<Position> findById(String s) throws IllegalArgumentException {
    if (s == null || s.trim().isEmpty()) {
      logger.warn("PositionDao.findById called with null/blank symbol");
      throw new IllegalArgumentException("Symbol is null/blank");
    }

    String ticker = s.trim().toUpperCase();
    logger.info("Finding position in DB symbol={}", ticker);

    String sql = "SELECT symbol, number_of_shares, value_paid FROM position WHERE symbol = ?";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ticker);
      ResultSet rs = ps.executeQuery();

      if (!rs.next()) {
        logger.warn("Position not found ticker={}", ticker);
        return Optional.empty();
      }

      Position p = resultsToPosition(rs);
      logger.debug("Position found ticker={} shares={} valuePaid={}",
          p.getTicker(), p.getNumOfShares(), p.getValuePaid());
      return Optional.of(p);
    } catch (SQLException e) {
      logger.error("DB error finding position ticker={}", ticker, e);
      throw new RuntimeException("Failed to find Position: " + ticker, e);
    }
  }

  @Override
  public Iterable<Position> findAll() {
    logger.info("Finding all positions");

    String sql = "SELECT symbol, number_of_shares, value_paid FROM position";

    List<Position> positions = new ArrayList<>();
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        positions.add(resultsToPosition(rs));
      }

      logger.debug("PositionDao.findAll returnedCount={}", positions.size());
      return positions;
    } catch (SQLException e) {
      logger.error("DB error finding all positions", e);
      throw new RuntimeException("Unable to find all Positions", e);
    }
  }

  @Override
  public void deleteById(String s) throws IllegalArgumentException {
    if (s == null || s.trim().isEmpty()) {
      logger.warn("PositionDao.deleteById called with null/blank symbol");
      throw new IllegalArgumentException("Symbol is null/blank");
    }

    String ticker = s.trim().toUpperCase();
    logger.info("Deleting position by DB symbol={}", ticker);

    String sql = "DELETE FROM position WHERE symbol = ?";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ticker);
      int rows = ps.executeUpdate();
      logger.debug("PositionDao.deleteById affectedRows={} ticker={}", rows, ticker);
    } catch (SQLException e) {
      logger.error("DB error deleting position ticker={}", ticker, e);
      throw new RuntimeException("Unable to delete Position: " + ticker, e);
    }
  }

  @Override
  public void deleteAll() {
    logger.info("Deleting all positions");

    String sql = "DELETE FROM position";

    try (PreparedStatement ps = c.prepareStatement(sql)) {
      int rows = ps.executeUpdate();
      logger.debug("PositionDao.deleteAll affectedRows={}", rows);
    } catch (SQLException e) {
      logger.error("DB error deleting all positions", e);
      throw new RuntimeException("Unable to delete all Positions", e);
    }
  }
}
