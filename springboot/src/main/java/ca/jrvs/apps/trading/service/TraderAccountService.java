package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.PositionDao;
import ca.jrvs.apps.trading.dao.SecurityOrderDao;
import ca.jrvs.apps.trading.dao.TraderDao;
import ca.jrvs.apps.trading.model.Account;
import ca.jrvs.apps.trading.model.Position;
import ca.jrvs.apps.trading.model.Trader;
import ca.jrvs.apps.trading.model.TraderAccountView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TraderAccountService {

  private TraderDao traderDao;
  private AccountDao accountDao;
  private PositionDao positionDao;
  private SecurityOrderDao securityOrderDao;

  @Autowired
  public TraderAccountService(TraderDao traderDao, AccountDao accountDao, PositionDao positionDao,
      SecurityOrderDao securityOrderDao) {
    this.traderDao = traderDao;
    this.accountDao = accountDao;
    this.positionDao = positionDao;
    this.securityOrderDao = securityOrderDao;
  }

  /**
   * Create a new trader and initialize a new account with 0 amount.
   */
  public TraderAccountView createTraderAndAccount(Trader trader) {
    validateTrader(trader);
    if (trader.getId() != null) {
      throw new IllegalArgumentException("Trader id must be null");
    }

    Trader savedTrader = traderDao.save(trader);
    Account account = new Account();
    account.setId(savedTrader.getId());
    account.setTraderId(savedTrader.getId());
    account.setAmount(0d);
    Account savedAccount = accountDao.save(account);

    TraderAccountView view = new TraderAccountView();
    view.setTraderId(savedTrader.getId());
    view.setFirstName(savedTrader.getFirstName());
    view.setLastName(savedTrader.getLastName());
    view.setDob(savedTrader.getDob());
    view.setCountry(savedTrader.getCountry());
    view.setEmail(savedTrader.getEmail());
    view.setAccountId(savedAccount.getId());
    view.setAmount(savedAccount.getAmount());
    return view;
  }

  /**
   * A trader can be deleted if and only if it has no open position and 0 cash balance.
   */
  public void deleteTraderById(Integer traderId) {
    if (traderId == null) {
      throw new IllegalArgumentException("Trader id is required");
    }

    Trader trader = traderDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Trader not found: " + traderId));
    Account account = accountDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + traderId));

    if (account.getAmount() != null && account.getAmount() > 0d) {
      throw new IllegalArgumentException("Account balance is not zero");
    }

    List<Position> positions = positionDao.findByAccountId(traderId);
    for (Position position : positions) {
      if (position.getPosition() != null && position.getPosition() != 0L) {
        throw new IllegalArgumentException("Account has open positions");
      }
    }

    securityOrderDao.deleteByAccountId(traderId);
    accountDao.deleteById(account.getId());
    traderDao.deleteById(trader.getId());
  }

  /**
   * Deposit a fund to an account by traderId.
   */
  public Account deposit(Integer traderId, Double fund) {
    if (traderId == null) {
      throw new IllegalArgumentException("Trader id is required");
    }
    if (fund == null || fund <= 0d) {
      throw new IllegalArgumentException("Fund must be greater than 0");
    }

    Account account = accountDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + traderId));
    Double amount = account.getAmount() == null ? 0d : account.getAmount();
    account.setAmount(amount + fund);
    return accountDao.save(account);
  }

  /**
   * Withdraw a fund to an account by traderId.
   */
  public Account withdraw(Integer traderId, Double fund) {
    if (traderId == null) {
      throw new IllegalArgumentException("Trader id is required");
    }
    if (fund == null || fund <= 0d) {
      throw new IllegalArgumentException("Fund must be greater than 0");
    }

    Account account = accountDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + traderId));
    Double amount = account.getAmount() == null ? 0d : account.getAmount();
    if (amount < fund) {
      throw new IllegalArgumentException("Insufficient account balance");
    }
    account.setAmount(amount - fund);
    return accountDao.save(account);
  }

  private void validateTrader(Trader trader) {
    if (trader == null) {
      throw new IllegalArgumentException("Trader is required");
    }
    if (isBlank(trader.getFirstName())
        || isBlank(trader.getLastName())
        || trader.getDob() == null
        || isBlank(trader.getCountry())
        || isBlank(trader.getEmail())) {
      throw new IllegalArgumentException("Trader fields are required");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
