package ca.jrvs.apps.trading.controller;

import ca.jrvs.apps.trading.model.Trader;
import ca.jrvs.apps.trading.model.TraderAccountView;
import ca.jrvs.apps.trading.service.DashboardService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  @Autowired
  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping(path = "/traders")
  @ResponseStatus(HttpStatus.OK)
  public List<Trader> getAllTraders() {
    try {
      return dashboardService.findAllTraders();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error: please contact admin", e);
    }
  }

  @GetMapping(path = "/profile/traderId/{traderId}")
  @ResponseStatus(HttpStatus.OK)
  public TraderAccountView getTraderProfile(@PathVariable Integer traderId) {
    try {
      return dashboardService.getTraderAccountView(traderId);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error: please contact admin", e);
    }
  }
}

