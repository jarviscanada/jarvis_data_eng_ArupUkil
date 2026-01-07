package ca.jrvs.apps.stockquote.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.jrvs.apps.stockquote.model.Position;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PositionDaoTest {
  @Mock
  private Connection c;
  @Mock
  private PreparedStatement ps;
  @Mock
  private ResultSet rs;

  private PositionDao dao;
  private Position msft;
  private Position aapl;

  @BeforeEach
  void setup() {
    dao = new PositionDao(c);

    msft = new Position();
    msft.setTicker("MSFT");
    msft.setNumOfShares(10);
    msft.setValuePaid(1000.0);

    aapl = new Position();
    aapl.setTicker("AAPL");
    aapl.setNumOfShares(5);
    aapl.setValuePaid(750.0);
  }

  private static void stubQuoteRows(ResultSet rs, List<Position> rows) throws SQLException {
    final int[] row = {-1}; // current cursor

    // next() advances row pointer
    when(rs.next()).thenAnswer(inv -> {
      row[0]++;
      return row[0] < rows.size();
    });

    // getters read from current row
    when(rs.getString("symbol")).thenAnswer(inv -> rows.get(row[0]).getTicker());
    when(rs.getInt("number_of_shares")).thenAnswer(inv -> rows.get(row[0]).getNumOfShares());
    when(rs.getDouble("value_paid")).thenAnswer(inv -> rows.get(row[0]).getValuePaid());
  }

  @Test
  public void testSave() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeUpdate()).thenReturn(1);

    Position out = dao.save(msft);

    assertSame(msft, out);

    // verify we prepared an INSERT/UPSERT statement
    ArgumentCaptor<String> sqlCap = ArgumentCaptor.forClass(String.class);
    verify(c).prepareStatement(sqlCap.capture());
    String sql = sqlCap.getValue().toLowerCase();
    assertTrue(sql.contains("insert into position"));

    // verify some key bindings
    verify(ps).setString(1, "MSFT");
    verify(ps).setInt(2, 10);
    verify(ps).setDouble(3, 1000.0);
    verify(ps).executeUpdate();
  }

  @Test
  public void testFindById() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);

    Optional<Position> out = dao.findById("MSFT");
    assertFalse(out.isPresent());

    verify(ps).setString(1, "MSFT");
    verify(ps).executeQuery();

    when(rs.next()).thenReturn(true);
    List<Position> rows = new ArrayList<>();
    rows.add(msft);
    stubQuoteRows(rs, rows);

    Optional<Position> out2 = dao.findById("MSFT");

    assertTrue(out2.isPresent());
    Position p = out2.get();
    assertEquals("MSFT", p.getTicker());
    assertEquals(10, p.getNumOfShares());
    assertEquals(1000.0, p.getValuePaid(), 1e-9);
  }

  @Test
  public void testFindAll() throws SQLException {
    when(c.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);

    // two rows then stop
    when(rs.next()).thenReturn(true, true, false);
    List<Position> rows = new ArrayList<>();
    rows.add(msft);
    rows.add(aapl);
    stubQuoteRows(rs, rows);

    Iterable<Position> it = dao.findAll();
    List<Position> list = new ArrayList<>();
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
