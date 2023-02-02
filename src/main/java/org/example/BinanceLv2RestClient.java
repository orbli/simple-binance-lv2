package org.example;

import org.example.data.BinanceLv2Snapshot;
import org.apache.hc.client5.http.fluent.Request;
import com.google.gson.Gson;

public class BinanceLv2RestClient implements Runnable {

    private final String symbol;
    private final BinanceLv2Digester digester;
    public boolean completes;

    public BinanceLv2RestClient(String symbol, BinanceLv2Digester digester) {
        this.symbol = symbol;
        this.digester = digester;
        completes = false;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        String uri = String.format("https://api.binance.com/api/v3/depth?symbol=%s&limit=1000", symbol);
        try {
            String content = Request.get(uri).execute().returnContent().toString();
            BinanceLv2Snapshot snapshot = gson.fromJson(content, BinanceLv2Snapshot.class);
            digester.accessOrderbook(snapshot, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        completes = true;
    }
}
