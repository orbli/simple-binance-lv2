package org.example;

import org.example.data.OrderBook;

import java.math.BigDecimal;
import java.util.Map;

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
        int size = 10;
        for (Map.Entry<BigDecimal, BigDecimal> entry : orderbook.bids.entrySet()) {
            if (entry.getKey().toString().length() > size) {
                size = entry.getKey().toString().length();
            }
            if (entry.getValue().toString().length() > size) {
                size = entry.getValue().toString().length();
            }
        }
        for (Map.Entry<BigDecimal, BigDecimal> entry : orderbook.asks.entrySet()) {
            if (entry.getKey().toString().length() > size) {
                size = entry.getKey().toString().length();
            }
            if (entry.getValue().toString().length() > size) {
                size = entry.getValue().toString().length();
            }
        }
        String format = "%-" + size + "s %" + size + "s %-" + size + "s %" + size + "s%n";
        System.out.printf(format, "BID_SIZE", "BID_PRICE", "ASK_PRICE", "ASK_SIZE");
        BigDecimal bid_price = orderbook.bids.lastKey();
        BigDecimal ask_price = orderbook.asks.firstKey();
        for (int i = 0; i < depth; i++) {
            if (i != 0) {
                if (bid_price != null) {
                    bid_price = orderbook.bids.lowerKey(bid_price);
                }
                if (ask_price != null) {
                    ask_price = orderbook.asks.higherKey(ask_price);
                }
                if (bid_price == null && ask_price == null) {
                    break;
                }
            }
            String bid_price_str = "";
            String bid_size_str = "";
            if (bid_price != null) {
                bid_price_str = bid_price.toString();
                bid_size_str = orderbook.bids.get(bid_price).toString();
            }
            String ask_price_str = "";
            String ask_size_str = "";
            if (ask_price != null) {
                ask_price_str = ask_price.toString();
                ask_size_str = orderbook.asks.get(ask_price).toString();
            }
            System.out.printf(format, bid_size_str, bid_price_str, ask_price_str, ask_size_str);
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
