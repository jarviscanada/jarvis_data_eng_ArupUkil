package ca.jrvs.apps.trading.model;

import java.io.Serializable;
import java.util.Objects;

public class PositionId implements Serializable {

  private Integer accountId;
  private String ticker;

  public PositionId() {}

  public PositionId(Integer accountId, String ticker) {
    this.accountId = accountId;
    this.ticker = ticker;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public String getTicker() {
    return ticker;
  }

  public void setTicker(String ticker) {
    this.ticker = ticker;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PositionId that = (PositionId) o;
    return Objects.equals(accountId, that.accountId)
        && Objects.equals(ticker, that.ticker);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, ticker);
  }
}
