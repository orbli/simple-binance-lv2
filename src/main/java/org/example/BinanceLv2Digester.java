package org.example;

import org.example.data.BinanceLv2Snapshot;
import org.example.data.BinanceLv2Update;
import org.example.data.OrderBook;

import java.util.ArrayList;

public class BinanceLv2Digester {
    private final BinanceLv2WssClient client;
    private long orderbook_update_number;
    private OrderBook orderbook;
    private final ArrayList<BinanceLv2Update> updateCache;
    private final String symbol;
    private long last_snapshot_request;

    public BinanceLv2Digester(String symbol) {
        this.symbol = symbol;
        String url = String.format("wss://stream.binance.com:9443/stream?streams=%s@depth@100ms", symbol.toLowerCase());
        this.client = new BinanceLv2WssClient(url, this);
        this.orderbook = null;
        this.orderbook_update_number = 0;
        updateCache = new ArrayList<>();
    }

    public void start() {
        client.connect();
    }

    public synchronized OrderBook accessOrderbook(BinanceLv2Snapshot snapshot, BinanceLv2Update update) {
        if (orderbook == null) {
            if (snapshot != null) {
                orderbook = snapshot.toOrderbook();
                orderbook_update_number = snapshot.lastUpdateId;
                if (
                        (updateCache.get(0).data.U - 1 <= snapshot.lastUpdateId) &&
                                (snapshot.lastUpdateId <= updateCache.get(updateCache.size() - 1).data.u)
                ) {
                    for (BinanceLv2Update updateCacheItem : updateCache) {
                        if (!(orderbook.update(updateCacheItem.toOrderBookUpdate()))) {
                            orderbook = null;
                            break;
                        }
                        orderbook_update_number = updateCacheItem.data.u;
                    }
                }
                if (snapshot.lastUpdateId < updateCache.get(0).data.u) {
                    orderbook = null;
                }
            }
            if (update != null) {
                updateCache.add(update);
                if (updateCache.size() > 1000) {
                    updateCache.remove(0);
                }
                if ((last_snapshot_request == 0) || (System.currentTimeMillis() - last_snapshot_request > 1000)) {
                    last_snapshot_request = System.currentTimeMillis();
                    // any green thread?
                    new Thread(new BinanceLv2RestClient(symbol, this)).start();
                }
            }
        }
        if (orderbook != null) {
            if (snapshot != null) {
                if (snapshot.lastUpdateId < orderbook_update_number) {
                    orderbook = snapshot.toOrderbook();
                    orderbook_update_number = snapshot.lastUpdateId;
                }
            }
            if (update != null) {
                if ((update.data.U - 1 <= orderbook_update_number) && (orderbook_update_number < update.data.u)) {
                    orderbook_update_number = update.data.u;
                    if (!(orderbook.update(update.toOrderBookUpdate()))) {
                        this.orderbook = null;
                    }
                } else {
                    this.orderbook = null;
                }
            }
        }
        return orderbook;
    }
}
