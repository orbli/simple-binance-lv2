package org.example.data;

import java.math.BigDecimal;
import java.util.TreeMap;

public final class BinanceLv2Snapshot {
    public long lastUpdateId;
    public BigDecimal[][] bids;
    public BigDecimal[][] asks;

    public OrderBook toOrderbook() {
        TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>();
        for (BigDecimal[] bid : this.bids) {
            bids.put(bid[0], bid[1]);
        }
        TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();
        for (BigDecimal[] ask : this.asks) {
            asks.put(ask[0], ask[1]);
        }
        return new OrderBook(bids, asks);
    }
}
