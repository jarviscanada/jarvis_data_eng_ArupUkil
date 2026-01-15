package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.Account;
import ca.jrvs.apps.trading.model.Position;
import ca.jrvs.apps.trading.model.PositionId;
import ca.jrvs.apps.trading.model.SecurityOrder;
import ca.jrvs.apps.trading.model.Trader;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class PositionDaoIntTest {

  @Autowired
  private PositionDao positionDao;

  @Autowired
  private SecurityOrderDao securityOrderDao;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private TraderDao traderDao;

  private Trader savedTrader;
  private Account savedAccount;

  @Before
  public void setup() {
    cleanup();
    savedTrader = buildTrader();
    savedTrader = traderDao.save(savedTrader);
    savedAccount = buildAccount(savedTrader.getId());
    savedAccount = accountDao.save(savedAccount);
    SecurityOrder order = buildOrder(savedAccount.getId(), "AAPL");
    securityOrderDao.save(order);
  }

  @After
  public void tearDown() {
    cleanup();
  }

  @Test
  public void findById() {
    PositionId id = new PositionId(savedAccount.getId(), "AAPL");
    Position position = positionDao.findById(id).orElse(null);
    assertTrue(position != null && position.getPosition() != null);
  }

  @Test
  public void findAll() {
    List<Position> positions = positionDao.findAll();
    assertTrue(positions.size() >= 1);
  }

  @Test
  public void findAllById() {
    PositionId id = new PositionId(savedAccount.getId(), "AAPL");
    Iterable<Position> positions = positionDao.findAllById(Arrays.asList(id));
    int count = 0;
    for (Position p : positions) {
      count++;
    }
    assertEquals(1, count);
  }

  @Test
  public void findByAccountId() {
    List<Position> positions = positionDao.findByAccountId(savedAccount.getId());
    assertTrue(positions.size() >= 1);
  }

  @Test
  public void existsById() {
    PositionId id = new PositionId(savedAccount.getId(), "AAPL");
    assertTrue(positionDao.existsById(id));
  }

  @Test
  public void count() {
    assertTrue(positionDao.count() >= 1);
  }

  private void cleanup() {
    securityOrderDao.deleteAll();
    accountDao.deleteAll();
    traderDao.deleteAll();
  }

  private Trader buildTrader() {
    Trader trader = new Trader();
    trader.setFirstName("Alice");
    trader.setLastName("Lee");
    trader.setDob(Date.valueOf("1990-01-01"));
    trader.setCountry("Canada");
    trader.setEmail("alice@example.com");
    return trader;
  }

  private Account buildAccount(Integer traderId) {
    Account account = new Account();
    account.setId(traderId);
    account.setTraderId(traderId);
    account.setAmount(100d);
    return account;
  }

  private SecurityOrder buildOrder(Integer accountId, String ticker) {
    SecurityOrder order = new SecurityOrder();
    order.setAccountId(accountId);
    order.setStatus("FILLED");
    order.setTicker(ticker);
    order.setSize(10);
    order.setPrice(100d);
    order.setNotes("test");
    order.setSide("BUY");
    order.setType("MARKET");
    return order;
  }
}
