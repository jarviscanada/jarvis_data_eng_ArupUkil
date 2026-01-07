package ca.jrvs.apps.stockquote.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.jrvs.apps.stockquote.model.Quote;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuoteDaoTest {
  @Mock
  private Connection c;
  @Mock
  private PreparedStatement ps;
  @Mock
  private ResultSet rs;

  private QuoteDao dao;
  private Quote msft;
  private Quote aapl;

  @BeforeEach
  void setup() {
    dao = new QuoteDao(c);
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

    aapl = new Quote();
    aapl.setTicker("AAPL");
    aapl.setOpen(200);
    aapl.setHigh(210);
    aapl.setLow(190);
    aapl.setPrice(205);
    aapl.setVolume(500);
    aapl.setLatestTradingDay(Date.valueOf("2023-10-13"));
    aapl.setPreviousClose(201);
    aapl.setChange(4);
    aapl.setChangePercent("1.99%");
    aapl.setTimestamp(new Timestamp(System.currentTimeMillis()));
  }

  private static void stubQuoteRows(ResultSet rs, List<Quote> rows) throws SQLException {
    final int[] row = {-1}; // current cursor

    // next() advances row pointer
    when(rs.next()).thenAnswer(inv -> {
      row[0]++;
      return row[0] < rows.size();
    });

    // getters read from current row
    when(rs.getString("symbol")).thenAnswer(inv -> rows.get(row[0]).getTicker());
    when(rs.getDouble("open")).thenAnswer(inv -> rows.get(row[0]).getOpen());
    when(rs.getDouble("high")).thenAnswer(inv -> rows.get(row[0]).getHigh());
    when(rs.getDouble("low")).thenAnswer(inv -> rows.get(row[0]).getLow());
    when(rs.getDouble("price")).thenAnswer(inv -> rows.get(row[0]).getPrice());
    when(rs.getInt("volume")).thenAnswer(inv -> rows.get(row[0]).getVolume());
    when(rs.getDate("latest_trading_day")).thenAnswer(inv -> rows.get(row[0]).getLatestTradingDay());
    when(rs.getDouble("previous_close")).thenAnswer(inv -> rows.get(row[0]).getPreviousClose());
    when(rs.getDouble("change")).thenAnswer(inv -> rows.get(row[0]).getChange());
    when(rs.getString("change_percent")).thenAnswer(inv -> rows.get(row[0]).getChangePercent());
    when(rs.getTimestamp("timestamp")).thenAnswer(inv -> rows.get(row[0]).getTimestamp());
  }

  @Test
  public void testSave() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeUpdate()).thenReturn(1);

    Quote out = dao.save(msft);

    assertSame(msft, out);

    // verify we prepared an INSERT/UPSERT statement
    ArgumentCaptor<String> sqlCap = ArgumentCaptor.forClass(String.class);
    verify(c).prepareStatement(sqlCap.capture());
    String sql = sqlCap.getValue().toLowerCase();
    assertTrue(sql.contains("insert into quote"));
    assertTrue(sql.contains("symbol"));

    // verify some key bindings
    verify(ps).setString(1, "MSFT");
    verify(ps).setDouble(2, 100.0);
    verify(ps).setInt(6, 1000);
    verify(ps).setDate(7, Date.valueOf("2023-10-13"));
    verify(ps).setString(10, "3.96%");

    verify(ps).executeUpdate();
  }

  @Test
  public void testFindById() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);

    Optional<Quote> out = dao.findById("MSFT");

    assertFalse(out.isPresent());
    verify(ps).setString(1, "MSFT");
    verify(ps).executeQuery();

    when(rs.next()).thenReturn(true);
    List<Quote> rows = new ArrayList<>();
    rows.add(msft);
    stubQuoteRows(rs, rows);

    Optional<Quote> out2 = dao.findById("MSFT");

    assertTrue(out2.isPresent());
    Quote q = out2.get();
    assertEquals("MSFT", q.getTicker());
    assertEquals(105.0, q.getPrice(), 1e-9);
    assertEquals(1000, q.getVolume());
    assertEquals("3.96%", q.getChangePercent());
  }

  @Test
  public void testFindAll() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);

    // two rows then stop
    when(rs.next()).thenReturn(true, true, false);
    List<Quote> rows = new ArrayList<>();
    rows.add(msft);
    rows.add(aapl);
    stubQuoteRows(rs, rows);

    Iterable<Quote> it = dao.findAll();
    List<Quote> list = new ArrayList<>();
    it.forEach(list::add);

    assertEquals(2, list.size());
    assertEquals("MSFT", list.get(0).getTicker());
    assertEquals("AAPL", list.get(1).getTicker());
  }

  @Test
  public void testDeleteById() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeUpdate()).thenReturn(1);

    dao.deleteById("MSFT");

    verify(ps).setString(1, "MSFT");
    verify(ps).executeUpdate();
  }

  @Test
  public void testDeleteAll() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeUpdate()).thenReturn(2);

    dao.deleteAll();

    verify(ps).executeUpdate();
  }
}
