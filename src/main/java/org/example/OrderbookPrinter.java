package org.example;

import org.example.data.OrderBook;

import java.math.BigDecimal;

public class OrderbookPrinter implements Runnable {

    private final BinanceLv2Digester digester;

    public OrderbookPrinter(BinanceLv2Digester digester) {
        this.digester = digester;
    }

    public static void printOrderbook(OrderBook orderbook, Integer depth) {
        if (depth == null) {
            depth = 10;
        }
        System.out.println("BID_SIZE BID_PRICE ASK_PRICE ASK_SIZE");
        BigDecimal bid_price = orderbook.bids.lastKey();
        BigDecimal ask_price = orderbook.asks.firstKey();
        for (int i = 0; i < depth; i++) {
            if (i != 0) {
                bid_price = orderbook.bids.lowerKey(bid_price);
                ask_price = orderbook.asks.higherKey(ask_price);
            }
            BigDecimal bid_size = orderbook.bids.get(bid_price);
            BigDecimal ask_size = orderbook.asks.get(ask_price);
            System.out.printf("%s %s %s %s%n", bid_size, bid_price, ask_price, ask_size);
        }
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    // this is a requirement
    @Override
    public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OrderBook orderbook = digester.accessOrderbook(null, null);
                if (orderbook != null) {
                    printOrderbook(orderbook, 10);
                } else {
                    System.out.println("orderbook is null");
                }
            }
    }
}
