package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.TestConfig;
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
public class TraderDaoIntTest {

  @Autowired
  private TraderDao traderDao;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private SecurityOrderDao securityOrderDao;

  private Trader savedTrader;

  @Before
  public void setup() {
    cleanup();
    savedTrader = buildTrader("Amy", "Zhang");
    savedTrader = traderDao.save(savedTrader);
  }

  @After
  public void tearDown() {
    cleanup();
  }

  @Test
  public void save() {
    Trader trader = buildTrader("Bob", "Smith");
    traderDao.save(trader);
    assertTrue(traderDao.findAll().size() >= 2);
  }

  @Test
  public void saveAll() {
    Trader trader = buildTrader("Bob", "Smith");
    traderDao.saveAll(Arrays.asList(savedTrader, trader));
    assertTrue(traderDao.findAll().size() >= 2);
  }

  @Test
  public void findAll() {
    List<Trader> traders = traderDao.findAll();
    assertTrue(traders.size() >= 1);
  }

  @Test
  public void findById() {
    Trader trader = traderDao.findById(savedTrader.getId()).orElse(null);
    assertTrue(trader != null && trader.getId().equals(savedTrader.getId()));
  }

  @Test
  public void existsById() {
    assertTrue(traderDao.existsById(savedTrader.getId()));
  }

  @Test
  public void findAllById() {
    List<Trader> traders = traderDao.findAllById(Arrays.asList(savedTrader.getId(), -1));
    assertEquals(1, traders.size());
  }

  @Test
  public void deleteById() {
    traderDao.deleteById(savedTrader.getId());
    assertFalse(traderDao.existsById(savedTrader.getId()));
  }

  @Test
  public void count() {
    assertTrue(traderDao.count() >= 1);
  }

  @Test
  public void deleteAll() {
    traderDao.deleteAll();
    assertEquals(0, traderDao.count());
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
