package ca.jrvs.apps.trading.model;

public class MarketOrder {

  public enum Option {
    BUY,
    SELL
  }

  private String ticker;
  private int size;
  private Integer traderId;
  private Option option;

  public String getTicker() {
    return ticker;
  }

  public void setTicker(String ticker) {
    this.ticker = ticker;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Integer getTraderId() {
    return traderId;
  }

  public void setTraderId(Integer traderId) {
    this.traderId = traderId;
  }

  public Option getOption() {
    return option;
  }

  public void setOption(Option option) {
    this.option = option;
  }
}
