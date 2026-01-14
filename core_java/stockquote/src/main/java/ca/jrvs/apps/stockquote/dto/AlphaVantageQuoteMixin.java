package ca.jrvs.apps.stockquote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AlphaVantageQuoteMixin {

  @JsonProperty("01. symbol") abstract void setTicker(String v);

  @JsonProperty("02. open") abstract void setOpen(double v);
  @JsonProperty("03. high") abstract void setHigh(double v);
  @JsonProperty("04. low")  abstract void setLow(double v);
  @JsonProperty("05. price") abstract void setPrice(double v);

  @JsonProperty("06. volume") abstract void setVolume(int v);

  @JsonProperty("07. latest trading day")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  abstract void setLatestTradingDay(Date v);

  @JsonProperty("08. previous close") abstract void setPreviousClose(double v);
  @JsonProperty("09. change") abstract void setChange(double v);
  @JsonProperty("10. change percent") abstract void setChangePercent(String v);
}
