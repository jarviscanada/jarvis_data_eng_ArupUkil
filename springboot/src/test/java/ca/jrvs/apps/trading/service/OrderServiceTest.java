package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.PositionDao;
import ca.jrvs.apps.trading.dao.QuoteDao;
import ca.jrvs.apps.trading.dao.SecurityOrderDao;
import ca.jrvs.apps.trading.model.Account;
import ca.jrvs.apps.trading.model.MarketOrderDto;
import ca.jrvs.apps.trading.model.Position;
import ca.jrvs.apps.trading.model.PositionId;
import ca.jrvs.apps.trading.model.Quote;
import ca.jrvs.apps.trading.model.SecurityOrder;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

  @Mock
  private AccountDao accountDao;

  @Mock
  private SecurityOrderDao securityOrderDao;

  @Mock
  private QuoteDao quoteDao;

  @Mock
  private PositionDao positionDao;

  @InjectMocks
  private OrderService orderService;

  @Captor
  private ArgumentCaptor<SecurityOrder> orderCaptor;

  @Captor
  private ArgumentCaptor<Account> accountCaptor;

  @Test
  public void executeMarketOrderBuy() {
    MarketOrderDto order = buildOrder("AAPL", 10, 1, MarketOrderDto.Option.BUY);
    Account account = buildAccount(1, 1000d);
    Quote quote = buildQuote("AAPL", 9d, 10d);

    when(accountDao.findById(1)).thenReturn(Optional.of(account));
    when(quoteDao.findById("AAPL")).thenReturn(Optional.of(quote));
    when(securityOrderDao.save(any(SecurityOrder.class))).thenAnswer(inv -> inv.getArgument(0));

    SecurityOrder saved = orderService.executeMarketOrder(order);

    assertNotNull(saved);
    verify(accountDao).save(accountCaptor.capture());
    verify(securityOrderDao).save(orderCaptor.capture());

    Account updated = accountCaptor.getValue();
    assertEquals(900d, updated.getAmount(), 0.0001);

    SecurityOrder so = orderCaptor.getValue();
    assertEquals("FILLED", so.getStatus());
    assertEquals("BUY", so.getSide());
    assertEquals("MARKET", so.getType());
    assertEquals(10d, so.getPrice(), 0.0001);
  }

  @Test
  public void executeMarketOrderBuyInsufficientFund() {
    MarketOrderDto order = buildOrder("AAPL", 10, 1, MarketOrderDto.Option.BUY);
    Account account = buildAccount(1, 50d);
    Quote quote = buildQuote("AAPL", 9d, 10d);

    when(accountDao.findById(1)).thenReturn(Optional.of(account));
    when(quoteDao.findById("AAPL")).thenReturn(Optional.of(quote));
    when(securityOrderDao.save(any(SecurityOrder.class))).thenAnswer(inv -> inv.getArgument(0));

    SecurityOrder saved = orderService.executeMarketOrder(order);

    assertNotNull(saved);
    verify(accountDao, never()).save(any(Account.class));
    verify(securityOrderDao).save(orderCaptor.capture());
    SecurityOrder so = orderCaptor.getValue();
    assertEquals("CANCELED", so.getStatus());
  }

  @Test
  public void executeMarketOrderSell() {
    MarketOrderDto order = buildOrder("AAPL", 5, 1, MarketOrderDto.Option.SELL);
    Account account = buildAccount(1, 100d);
    Quote quote = buildQuote("AAPL", 9d, 10d);
    Position position = new Position();
    position.setAccountId(1);
    position.setTicker("AAPL");
    position.setPosition(10L);

    when(accountDao.findById(1)).thenReturn(Optional.of(account));
    when(quoteDao.findById("AAPL")).thenReturn(Optional.of(quote));
    when(positionDao.findById(any(PositionId.class))).thenReturn(Optional.of(position));
    when(securityOrderDao.save(any(SecurityOrder.class))).thenAnswer(inv -> inv.getArgument(0));

    SecurityOrder saved = orderService.executeMarketOrder(order);

    assertNotNull(saved);
    verify(accountDao).save(accountCaptor.capture());
    Account updated = accountCaptor.getValue();
    assertEquals(145d, updated.getAmount(), 0.0001);

    verify(securityOrderDao).save(orderCaptor.capture());
    SecurityOrder so = orderCaptor.getValue();
    assertEquals("FILLED", so.getStatus());
    assertEquals("SELL", so.getSide());
    assertEquals(9d, so.getPrice(), 0.0001);
  }

  @Test
  public void executeMarketOrderSellInsufficientPosition() {
    MarketOrderDto order = buildOrder("AAPL", 5, 1, MarketOrderDto.Option.SELL);
    Account account = buildAccount(1, 100d);
    Quote quote = buildQuote("AAPL", 9d, 10d);
    Position position = new Position();
    position.setAccountId(1);
    position.setTicker("AAPL");
    position.setPosition(2L);

    when(accountDao.findById(1)).thenReturn(Optional.of(account));
    when(quoteDao.findById("AAPL")).thenReturn(Optional.of(quote));
    when(positionDao.findById(any(PositionId.class))).thenReturn(Optional.of(position));
    when(securityOrderDao.save(any(SecurityOrder.class))).thenAnswer(inv -> inv.getArgument(0));

    SecurityOrder saved = orderService.executeMarketOrder(order);

    assertNotNull(saved);
    verify(accountDao, never()).save(any(Account.class));
    verify(securityOrderDao).save(orderCaptor.capture());
    SecurityOrder so = orderCaptor.getValue();
    assertEquals("CANCELED", so.getStatus());
    assertEquals("Insufficient position", so.getNotes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeMarketOrderInvalidSize() {
    MarketOrderDto order = buildOrder("AAPL", 0, 1, MarketOrderDto.Option.BUY);
    orderService.executeMarketOrder(order);
  }

  private MarketOrderDto buildOrder(String ticker, int size, int traderId,
      MarketOrderDto.Option option) {
    MarketOrderDto order = new MarketOrderDto();
    order.setTicker(ticker);
    order.setSize(size);
    order.setTraderId(traderId);
    order.setOption(option);
    return order;
  }

  private Account buildAccount(int traderId, double amount) {
    Account account = new Account();
    account.setId(traderId);
    account.setTraderId(traderId);
    account.setAmount(amount);
    return account;
  }

  private Quote buildQuote(String ticker, Double bid, Double ask) {
    Quote quote = new Quote();
    quote.setTicker(ticker);
    quote.setBidPrice(bid);
    quote.setAskPrice(ask);
    quote.setLastPrice(ask);
    return quote;
  }
}
