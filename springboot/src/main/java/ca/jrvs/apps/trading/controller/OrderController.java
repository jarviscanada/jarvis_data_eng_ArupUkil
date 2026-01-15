package ca.jrvs.apps.trading.controller;

import ca.jrvs.apps.trading.model.MarketOrderDto;
import ca.jrvs.apps.trading.model.SecurityOrder;
import ca.jrvs.apps.trading.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/order")
public class OrderController {

  private OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping(path = "/marketOrder")
  @ResponseStatus(HttpStatus.CREATED)
  public SecurityOrder postMarketOrder(@RequestBody MarketOrderDto orderDto) {
    try {
      return orderService.executeMarketOrder(orderDto);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (DataAccessException e) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error: please contact admin", e);
    }
  }
}
