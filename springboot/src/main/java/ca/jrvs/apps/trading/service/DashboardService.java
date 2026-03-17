package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.TraderDao;
import ca.jrvs.apps.trading.model.Account;
import ca.jrvs.apps.trading.model.Trader;
import ca.jrvs.apps.trading.model.TraderAccountView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final TraderDao traderDao;
  private final AccountDao accountDao;

  @Autowired
  public DashboardService(TraderDao traderDao, AccountDao accountDao) {
    this.traderDao = traderDao;
    this.accountDao = accountDao;
  }

  public List<Trader> findAllTraders() {
    return traderDao.findAll();
  }

  public TraderAccountView getTraderAccountView(Integer traderId) {
    if (traderId == null) {
      throw new IllegalArgumentException("Trader id is required");
    }

    Trader trader = traderDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Trader not found: " + traderId));
    Account account = accountDao.findById(traderId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + traderId));

    TraderAccountView view = new TraderAccountView();
    view.setTraderId(trader.getId());
    view.setFirstName(trader.getFirstName());
    view.setLastName(trader.getLastName());
    view.setDob(trader.getDob());
    view.setCountry(trader.getCountry());
    view.setEmail(trader.getEmail());
    view.setAccountId(account.getId());
    view.setAmount(account.getAmount());
    return view;
  }
}

