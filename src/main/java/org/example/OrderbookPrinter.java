package org.example;

import org.example.data.OrderBook;

import java.math.BigDecimal;

public class OrderbookPrinter implements Runnable {

    private final BinanceLv2Digester digester;
    private final Integer depth;
    private final Integer time;

    public OrderbookPrinter(BinanceLv2Digester digester, Integer depth, Integer time) {
        this.digester = digester;
        this.depth = depth;
        this.time = time;
    }

    public static void printOrderbook(OrderBook orderbook, Integer depth) {
        if (depth == null) {
            depth = 10;
        }
        System.out.printf("%-15s %15s %-15s %15s%n", "BID_SIZE", "BID_PRICE", "ASK_PRICE", "ASK_SIZE");
        BigDecimal bid_price = orderbook.bids.lastKey();
        BigDecimal ask_price = orderbook.asks.firstKey();
        for (int i = 0; i < depth; i++) {
            if (i != 0) {
                bid_price = orderbook.bids.lowerKey(bid_price);
                ask_price = orderbook.asks.higherKey(ask_price);
            }
            BigDecimal bid_size = orderbook.bids.get(bid_price);
            BigDecimal ask_size = orderbook.asks.get(ask_price);
            System.out.printf("%-15s %15s %-15s %15s%n", bid_size, bid_price, ask_price, ask_size);
        }
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    // this is a requirement
    @Override
    public void run() {
            while (true) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OrderBook orderbook = digester.accessOrderbook(null, null);
                if (orderbook != null) {
                    printOrderbook(orderbook, depth);
                } else {
                    System.out.println("orderbook is null");
                }
            }
    }
}
