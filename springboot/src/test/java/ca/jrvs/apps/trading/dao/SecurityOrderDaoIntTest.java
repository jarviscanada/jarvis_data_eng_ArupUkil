package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.Account;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class SecurityOrderDaoIntTest {

  @Autowired
  private SecurityOrderDao securityOrderDao;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private TraderDao traderDao;

  private Trader savedTrader;
  private Account savedAccount;
  private SecurityOrder savedOrder;

  @Before
  public void setup() {
    cleanup();
    savedTrader = buildTrader();
    savedTrader = traderDao.save(savedTrader);
    savedAccount = buildAccount(savedTrader.getId());
    savedAccount = accountDao.save(savedAccount);
    savedOrder = buildOrder(savedAccount.getId(), "AAPL");
    savedOrder = securityOrderDao.save(savedOrder);
  }

  @After
  public void tearDown() {
    cleanup();
  }

  @Test
  public void save() {
    SecurityOrder order = buildOrder(savedAccount.getId(), "MSFT");
    securityOrderDao.save(order);
    assertTrue(securityOrderDao.findByAccountId(savedAccount.getId()).size() >= 2);
  }

  @Test
  public void saveAll() {
    SecurityOrder order = buildOrder(savedAccount.getId(), "MSFT");
    securityOrderDao.saveAll(Arrays.asList(savedOrder, order));
    assertTrue(securityOrderDao.findByAccountId(savedAccount.getId()).size() >= 2);
  }

  @Test
  public void findAll() {
    List<SecurityOrder> orders = securityOrderDao.findAll();
    assertTrue(orders.size() >= 1);
  }

  @Test
  public void findById() {
    SecurityOrder order = securityOrderDao.findById(savedOrder.getId()).orElse(null);
    assertTrue(order != null && order.getId().equals(savedOrder.getId()));
  }

  @Test
  public void existsById() {
    assertTrue(securityOrderDao.existsById(savedOrder.getId()));
  }

  @Test
  public void findAllById() {
    List<SecurityOrder> orders = securityOrderDao.findAllById(Arrays.asList(savedOrder.getId(), -1));
    assertEquals(1, orders.size());
  }

  @Test
  public void deleteById() {
    securityOrderDao.deleteById(savedOrder.getId());
    assertFalse(securityOrderDao.existsById(savedOrder.getId()));
  }

  @Test
  public void count() {
    assertTrue(securityOrderDao.count() >= 1);
  }

  @Test
  public void deleteAll() {
    securityOrderDao.deleteAll();
    assertEquals(0, securityOrderDao.count());
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
