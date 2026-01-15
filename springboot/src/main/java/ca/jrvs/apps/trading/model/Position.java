package ca.jrvs.apps.trading.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "position")
@IdClass(PositionId.class)
@Immutable
public class Position {

  @Id
  @Column(name = "account_id")
  private Integer accountId;

  @Id
  @Column(name = "ticker")
  private String ticker;

  @Column(name = "position")
  private Long position;

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

  public Long getPosition() {
    return position;
  }

  public void setPosition(Long position) {
    this.position = position;
  }
}
