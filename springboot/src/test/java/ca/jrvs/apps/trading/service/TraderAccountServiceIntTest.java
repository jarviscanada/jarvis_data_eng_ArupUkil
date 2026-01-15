package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.SecurityOrderDao;
import ca.jrvs.apps.trading.dao.TraderDao;
import ca.jrvs.apps.trading.model.Account;
import ca.jrvs.apps.trading.model.SecurityOrder;
import ca.jrvs.apps.trading.model.Trader;
import ca.jrvs.apps.trading.model.TraderAccountView;
import java.sql.Date;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class TraderAccountServiceIntTest {

  @Autowired
  private TraderAccountService traderAccountService;

  @Autowired
  private TraderDao traderDao;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private SecurityOrderDao securityOrderDao;

  @Before
  public void setup() {
    cleanup();
  }

  @After
  public void tearDown() {
    cleanup();
  }

  @Test
  public void createTraderAndAccount() {
    Trader trader = buildTrader("Amy", "Zhang");
    TraderAccountView view = traderAccountService.createTraderAndAccount(trader);
    assertNotNull(view.getTraderId());
    assertEquals(0d, view.getAmount(), 0.0001);
    assertTrue(traderDao.existsById(view.getTraderId()));
    assertTrue(accountDao.existsById(view.getAccountId()));
  }

  @Test
  public void depositAndWithdraw() {
    Trader trader = buildTrader("Bob", "Smith");
    TraderAccountView view = traderAccountService.createTraderAndAccount(trader);

    Account deposited = traderAccountService.deposit(view.getTraderId(), 100d);
    assertEquals(100d, deposited.getAmount(), 0.0001);

    Account withdrawn = traderAccountService.withdraw(view.getTraderId(), 40d);
    assertEquals(60d, withdrawn.getAmount(), 0.0001);
  }

  @Test
  public void deleteTraderById() {
    Trader trader = buildTrader("Cara", "Lee");
    TraderAccountView view = traderAccountService.createTraderAndAccount(trader);

    traderAccountService.deleteTraderById(view.getTraderId());
    assertFalse(traderDao.existsById(view.getTraderId()));
    assertFalse(accountDao.existsById(view.getAccountId()));
  }

  @Test
  public void deleteTraderByIdWithPosition() {
    Trader trader = buildTrader("Dan", "Ng");
    TraderAccountView view = traderAccountService.createTraderAndAccount(trader);

    SecurityOrder order = new SecurityOrder();
    order.setAccountId(view.getAccountId());
    order.setStatus("FILLED");
    order.setTicker("AAPL");
    order.setSize(10);
    order.setPrice(100d);
    order.setNotes("test");
    order.setSide("BUY");
    order.setType("MARKET");
    securityOrderDao.save(order);

    try {
      traderAccountService.deleteTraderById(view.getTraderId());
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private void cleanup() {
    securityOrderDao.deleteAll();
    accountDao.deleteAll();
    traderDao.deleteAll();
  }

  private Trader buildTrader(String firstName, String lastName) {
    Trader trader = new Trader();
    trader.setFirstName(firstName);
    trader.setLastName(lastName);
    trader.setDob(Date.valueOf("1990-01-01"));
    trader.setCountry("Canada");
    trader.setEmail(firstName.toLowerCase() + "@example.com");
    return trader;
  }
}
