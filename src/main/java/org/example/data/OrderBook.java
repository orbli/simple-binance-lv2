package org.example.data;

import java.math.BigDecimal;
import java.util.TreeMap;

public class OrderBook {
    public TreeMap<BigDecimal, BigDecimal> bids;
    public TreeMap<BigDecimal, BigDecimal> asks;

    public OrderBook(TreeMap<BigDecimal, BigDecimal> bids, TreeMap<BigDecimal, BigDecimal> asks) {
        this.bids = bids;
        this.asks = asks;
    }

    /*
      @return if the update is applied successfully
    */
    public boolean update(OrderBookUpdate update) {
        for (BigDecimal price : update.asks.keySet()) {
            BigDecimal size = update.asks.get(price);
            if (size.compareTo(BigDecimal.ZERO) == 0) {
                asks.remove(price);
            } else {
                asks.put(price, size);
            }
        }
        for (BigDecimal price : update.bids.keySet()) {
            BigDecimal size = update.bids.get(price);
            if (size.compareTo(BigDecimal.ZERO) == 0) {
                bids.remove(price);
            } else {
                bids.put(price, size);
            }
        }
        return true;
    }
}
