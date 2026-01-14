package ca.jrvs.apps.stockquote.controller;

import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.service.PositionService;
import ca.jrvs.apps.stockquote.service.QuoteService;
import java.util.Optional;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockQuoteController {
  private static final Logger logger = LoggerFactory.getLogger(StockQuoteController.class);

  private QuoteService quoteService;
  private PositionService positionService;

  public StockQuoteController(QuoteService quoteService, PositionService positionService) {
    this.quoteService = quoteService;
    this.positionService = positionService;
  }

  /**
   * User interface for our application
   */
  public void initClient() {
    logger.info("Controller started");
    printHelp();

    try (Scanner scanner = new Scanner(System.in)) {
      while (true) {
        System.out.print("> "); // UI prompt is fine; requirement was no logging to console
        String line = scanner.nextLine();
        if (line == null) break;

        line = line.trim();
        if (line.isEmpty()) continue;

        String[] tokens = line.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        try {
          switch (cmd) {
            case "help":
              printHelp();
              break;

            case "exit":
            case "quit":
              logger.info("Controller exiting by user command");
              return;

            case "quote":
              // quote <TICKER>
              requireArgs(tokens, 2);
              handleQuote(tokens[1]);
              break;

            case "buy":
              // buy <TICKER> <SHARES>
              requireArgs(tokens, 3);
              handleBuy(tokens[1], tokens[2]);
              break;

            case "sell":
              // sell <TICKER>
              requireArgs(tokens, 2);
              handleSell(tokens[1]);
              break;

            case "positions":
              // positions
              requireArgs(tokens, 1);
              handlePositions();
              break;

            default:
              System.out.println("Unknown command. Type 'help'.");
              logger.warn("Unknown command line={}", line);
          }
        } catch (IllegalArgumentException e) {
          // expected validation issues
          System.out.println("Error: " + e.getMessage());
          logger.warn("User error cmd={} msg={}", cmd, e.getMessage());
        } catch (Exception e) {
          // unexpected issues
          System.out.println("Unexpected error. See logs.");
          logger.error("Unexpected error processing cmd={} line={}", cmd, line, e);
        }
      }
    }
  }

  /**
   * Outputs help info which is for users to understand how to use the app
   */
  private void printHelp() {
    System.out.println("Commands:");
    System.out.println("  quote <TICKER>                 Fetch latest quote from API and save to DB");
    System.out.println("  buy <TICKER> <SHARES>          Buy shares (volume checked using quote in DB)");
    System.out.println("  sell <TICKER>                  Sell all shares (delete position)");
    System.out.println("  positions                      List your positions");
    System.out.println("  help                           Show this help");
    System.out.println("  exit                           Quit");
  }

  private void handleQuote(String ticker) {
    // Fetch from API and save into DB
    Optional<Quote> qOpt = quoteService.fetchQuoteDataFromAPI(ticker);
    if (!qOpt.isPresent()) {
      System.out.println("No quote found for " + ticker);
      logger.warn("Quote not found ticker={}", ticker);
      return;
    }

    Quote q = qOpt.get();
    System.out.println("Quote saved:");
    System.out.println("  " + q.getTicker()
        + " price=" + q.getPrice()
        + " open=" + q.getOpen()
        + " high=" + q.getHigh()
        + " low=" + q.getLow()
        + " volume=" + q.getVolume()
        + " latestTradingDay=" + q.getLatestTradingDay());
    logger.info("Quote displayed ticker={}", q.getTicker());
  }

  private void handleBuy(String ticker, String sharesStr) {
    int shares;

    try {
      shares = Integer.parseInt(sharesStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid shares input : " + sharesStr);
    }

    Optional<Quote> qOpt = quoteService.fetchQuoteDataFromAPI(ticker);
    if (!qOpt.isPresent()) {
      System.out.println("No quote for " + ticker);
      return;
    }

    Quote q = qOpt.get();
    double price = q.getPrice();

    Position p = positionService.buy(ticker, shares, price);

    System.out.println("Bought. Current position:");
    System.out.println("  " + p.getTicker()
        + " shares=" + p.getNumOfShares()
        + " valuePaid=" + p.getValuePaid());
    logger.info("Buy complete ticker={} shares={} price={}", p.getTicker(), shares, price);
  }

  private void handleSell(String ticker) {
    positionService.sell(ticker);
    System.out.println("Sold (deleted) position for " + ticker.toUpperCase());
    logger.info("Sell complete ticker={}", ticker);
  }

  private void handlePositions() {
    Iterable<Position> positions = positionService.findAll();

    int count = 0;
    System.out.println("Positions:");
    for (Position p : positions) {
      count++;
      System.out.println("  " + p.getTicker()
          + " shares=" + p.getNumOfShares()
          + " valuePaid=" + p.getValuePaid());
    }

    if (count == 0) {
      System.out.println("  (none)");
    }

    logger.info("Listed positions count={}", count);
  }

  /**
   * Checks if the number of inputs match the expected amount
   * @param tokens the inputs into the cmd
   * @param expected the expected number of inputs
   */
  private void requireArgs(String[] tokens, int expected) {
    if (tokens.length != expected) {
      throw new IllegalArgumentException("Expected " + (expected - 1) + " argument(s). Type 'help'.");
    }
  }
}
