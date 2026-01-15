package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.Account;
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
public class AccountDaoIntTest {

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private TraderDao traderDao;

  @Autowired
  private SecurityOrderDao securityOrderDao;

  private Trader savedTrader;
  private Account savedAccount;

  @Before
  public void setup() {
    cleanup();
    savedTrader = buildTrader();
    savedTrader = traderDao.save(savedTrader);
    savedAccount = buildAccount(savedTrader.getId());
    savedAccount = accountDao.save(savedAccount);
  }

  @After
  public void tearDown() {
    cleanup();
  }

  @Test
  public void save() {
    Trader trader = buildTrader();
    trader = traderDao.save(trader);
    Account account = buildAccount(trader.getId());
    accountDao.save(account);
    assertTrue(accountDao.existsById(trader.getId()));
  }

  @Test
  public void saveAll() {
    Trader trader = buildTrader();
    trader = traderDao.save(trader);
    Account account = buildAccount(trader.getId());
    accountDao.saveAll(Arrays.asList(savedAccount, account));
    assertTrue(accountDao.existsById(trader.getId()));
  }

  @Test
  public void findAll() {
    List<Account> accounts = accountDao.findAll();
    assertTrue(accounts.size() >= 1);
  }

  @Test
  public void findById() {
    Account account = accountDao.findById(savedAccount.getId()).orElse(null);
    assertTrue(account != null && account.getId().equals(savedAccount.getId()));
  }

  @Test
  public void existsById() {
    assertTrue(accountDao.existsById(savedAccount.getId()));
  }

  @Test
  public void findAllById() {
    List<Account> accounts = accountDao.findAllById(Arrays.asList(savedAccount.getId(), -1));
    assertEquals(1, accounts.size());
  }

  @Test
  public void deleteById() {
    accountDao.deleteById(savedAccount.getId());
    assertFalse(accountDao.existsById(savedAccount.getId()));
  }

  @Test
  public void count() {
    assertTrue(accountDao.count() >= 1);
  }

  @Test
  public void deleteAll() {
    accountDao.deleteAll();
    assertEquals(0, accountDao.count());
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
}
