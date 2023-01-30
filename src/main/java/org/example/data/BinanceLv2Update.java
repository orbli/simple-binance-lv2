package org.example.data;

import java.math.BigDecimal;
import java.util.TreeMap;

public final class BinanceLv2Update {
//    public String stream;
    public BinanceLv2Data data;

    public final static class BinanceLv2Data {
//        public String e; // event type
//        public long E; // event time
//        public String s; // symbol
        public BigDecimal[][] b;
        public BigDecimal[][] a;
        public long u; // final update id in event (doc mislead)
        public long U; // first update id in event (doc mislead)
    }

    public OrderBookUpdate toOrderBookUpdate() {
        OrderBookUpdate update = new OrderBookUpdate();
        TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>();
        for (BigDecimal[] bid : data.b) {
            bids.put(bid[0], bid[1]);
        }
        TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();
        for (BigDecimal[] ask : data.a) {
            asks.put(ask[0], ask[1]);
        }
        update.bids = bids;
        update.asks = asks;
        return update;
    }
}
