package org.example;

import org.example.data.BinanceLv2Snapshot;
import org.example.data.BinanceLv2Update;
import org.example.data.OrderBook;
import org.slf4j.Logger;

import java.util.ArrayList;

public class BinanceLv2Digester {
    private final BinanceLv2WssClient client;
    private final Logger logger;
    private long orderbook_update_id;
    private OrderBook orderbook;
    private final ArrayList<BinanceLv2Update> updateCache;
    private final String symbol;
    private long last_snapshot_request;

    public BinanceLv2Digester(Logger logger, String symbol) {
        this.symbol = symbol;
        String url = String.format("wss://stream.binance.com:9443/stream?streams=%s@depth@100ms", symbol.toLowerCase());
        this.client = new BinanceLv2WssClient(url, this);
        this.orderbook = null;
        this.orderbook_update_id = 0;
        updateCache = new ArrayList<>();
        this.logger = logger;
    }

    public void start() {
//        TODO: reconnect mechanism?
        client.connect();
    }

    public synchronized OrderBook accessOrderbook(BinanceLv2Snapshot snapshot, BinanceLv2Update update) {
        if (snapshot == null && update == null) {
            return orderbook;
        }
        if (snapshot != null && update != null) {
            throw new IllegalArgumentException("snapshot and update cannot be both not null");
        }
        if (orderbook == null) {
            if (snapshot != null) {
                if (updateCache.size() == 0) {
                    logger.warn("snapshot is arrived before any update, ignoring snapshot");
                    return null;
                }
                orderbook = snapshot.toOrderbook();
                orderbook_update_id = snapshot.lastUpdateId;
//                snapshot lies between the first and the last update in the cache
                if (
                        (updateCache.get(0).data.U - 1 <= snapshot.lastUpdateId) &&
                                (snapshot.lastUpdateId <= updateCache.get(updateCache.size() - 1).data.u)
                ) {
                    for (BinanceLv2Update updateCacheItem : updateCache) {
                        if (!(orderbook.update(updateCacheItem.toOrderBookUpdate()))) {
                            orderbook = null;
                            break;
                        }
                        orderbook_update_id = updateCacheItem.data.u;
                    }
                }
//                snapshot is older than the first update in the cache
                if (snapshot.lastUpdateId < updateCache.get(0).data.u) {
                    orderbook = null;
                }
//                snapshot is newer than the last update in the cache - ignore snapshot
            } else { // if (update != null) {
                if (updateCache.size() > 0) {
                    if (updateCache.get(updateCache.size() - 1).data.u < update.data.U - 1) {
                        logger.warn("update has gap, rebuilding cache");
                        updateCache.clear();
                        return null;
                    }
                }
                updateCache.add(update);
//                keep cache size limited
                if (updateCache.size() > 1000) {
                    updateCache.remove(0);
                }
//                request for snapshot upon update & null orderbook; limiting request rate
                if ((last_snapshot_request == 0) || (System.currentTimeMillis() - last_snapshot_request > 1000)) {
                    last_snapshot_request = System.currentTimeMillis();
                    // any green thread?
                    new Thread(new BinanceLv2RestClient(symbol, this)).start();
                }
            }
        } else { // if (orderbook != null) {
            if (snapshot != null) {
//                snapshot is newer than built book: replace book by snapshot
                if (snapshot.lastUpdateId < orderbook_update_id) {
                    orderbook = snapshot.toOrderbook();
                    orderbook_update_id = snapshot.lastUpdateId;
                }
            } else { // if (update != null) {
//                range of update is within built book: update book
//                lemma: replaying old update will not corrupt the book
                if ((update.data.U - 1 <= orderbook_update_id) && (orderbook_update_id < update.data.u)) {
                    orderbook_update_id = update.data.u;
                    if (!(orderbook.update(update.toOrderBookUpdate()))) {
                        this.orderbook = null;
                    }
                } else {
//                    range of update is outside built book: book is invalid
                    this.orderbook = null;
                }
            }
        }
        return orderbook;
    }
}
