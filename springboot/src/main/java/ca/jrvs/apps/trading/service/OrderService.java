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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private static final String STATUS_FILLED = "FILLED";
  private static final String STATUS_CANCELED = "CANCELED";
  private static final String TYPE_MARKET = "MARKET";

  private AccountDao accountDao;
  private SecurityOrderDao securityOrderDao;
  private QuoteDao quoteDao;
  private PositionDao positionDao;

  @Autowired
  public OrderService(AccountDao accountDao, SecurityOrderDao securityOrderDao, QuoteDao quoteDao,
      PositionDao positionDao) {
    this.accountDao = accountDao;
    this.securityOrderDao = securityOrderDao;
    this.quoteDao = quoteDao;
    this.positionDao = positionDao;
  }

  /**
   * Execute a market order.
   *
   * @param orderData market order
   * @return SecurityOrder from security_order table
   * @throws DataAccessException if unable to get data from DAO
   * @throws IllegalArgumentException for invalid inputs
   */
  public SecurityOrder executeMarketOrder(MarketOrderDto orderData) {
    validateOrder(orderData);

    Account account = accountDao.findById(orderData.getTraderId())
        .orElseThrow(() -> new IllegalArgumentException("Account not found: "
            + orderData.getTraderId()));
    Quote quote = quoteDao.findById(orderData.getTicker())
        .orElseThrow(() -> new IllegalArgumentException("Ticker not found: "
            + orderData.getTicker()));

    SecurityOrder securityOrder = new SecurityOrder();
    securityOrder.setAccountId(account.getId());
    securityOrder.setTicker(orderData.getTicker());
    securityOrder.setSize(orderData.getSize());
    securityOrder.setSide(orderData.getOption().name());
    securityOrder.setType(TYPE_MARKET);

    if (orderData.getOption() == MarketOrderDto.Option.BUY) {
      handleBuyMarketOrder(orderData, securityOrder, account, quote);
    } else {
      handleSellMarketOrder(orderData, securityOrder, account, quote);
    }

    return securityOrderDao.save(securityOrder);
  }

  /**
   * Helper method to execute a buy order.
   */
  protected void handleBuyMarketOrder(MarketOrderDto marketOrder, SecurityOrder securityOrder,
      Account account, Quote quote) {
    double price = resolveBuyPrice(quote);
    securityOrder.setPrice(price);

    double amount = account.getAmount() == null ? 0d : account.getAmount();
    double required = price * marketOrder.getSize();
    if (amount < required) {
      securityOrder.setStatus(STATUS_CANCELED);
      securityOrder.setNotes("Insufficient fund");
      return;
    }

    account.setAmount(amount - required);
    accountDao.save(account);
    securityOrder.setStatus(STATUS_FILLED);
  }

  /**
   * Helper method to execute a sell order.
   */
  protected void handleSellMarketOrder(MarketOrderDto marketOrder, SecurityOrder securityOrder,
      Account account, Quote quote) {
    double price = resolveSellPrice(quote);
    securityOrder.setPrice(price);

    PositionId positionId = new PositionId(account.getId(), marketOrder.getTicker());
    Optional<Position> positionOpt = positionDao.findById(positionId);
    long available = positionOpt.map(Position::getPosition).orElse(0L);
    if (available < marketOrder.getSize()) {
      securityOrder.setStatus(STATUS_CANCELED);
      securityOrder.setNotes("Insufficient position");
      return;
    }

    double amount = account.getAmount() == null ? 0d : account.getAmount();
    account.setAmount(amount + price * marketOrder.getSize());
    accountDao.save(account);
    securityOrder.setStatus(STATUS_FILLED);
  }

  private void validateOrder(MarketOrderDto orderData) {
    if (orderData == null) {
      throw new IllegalArgumentException("Order is required");
    }
    if (orderData.getTraderId() == null) {
      throw new IllegalArgumentException("Trader id is required");
    }
    if (orderData.getTicker() == null || orderData.getTicker().trim().isEmpty()) {
      throw new IllegalArgumentException("Ticker is required");
    }
    if (orderData.getSize() <= 0) {
      throw new IllegalArgumentException("Order size must be greater than 0");
    }
    if (orderData.getOption() == null) {
      throw new IllegalArgumentException("Order option is required");
    }
  }

  private double resolveBuyPrice(Quote quote) {
    Double price = quote.getAskPrice();
    if (price == null) {
      price = quote.getLastPrice();
    }
    if (price == null || price <= 0d) {
      throw new IllegalArgumentException("Invalid ask price for ticker: " + quote.getTicker());
    }
    return price;
  }

  private double resolveSellPrice(Quote quote) {
    Double price = quote.getBidPrice();
    if (price == null) {
      price = quote.getLastPrice();
    }
    if (price == null || price <= 0d) {
      throw new IllegalArgumentException("Invalid bid price for ticker: " + quote.getTicker());
    }
    return price;
  }
}
