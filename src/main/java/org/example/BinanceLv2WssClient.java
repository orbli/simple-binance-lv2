package org.example;

import org.example.data.BinanceLv2Update;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import com.google.gson.Gson;

public class BinanceLv2WssClient extends WebSocketClient {
    private final BinanceLv2Digester digester;
    private final Gson gson = new Gson();

    public BinanceLv2WssClient(String url, BinanceLv2Digester digester) {
        super(URI.create(url));
        this.digester = digester;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen" + handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
//        TODO: malform / unexpected message?
        BinanceLv2Update update = gson.fromJson(message, BinanceLv2Update.class);
        this.digester.accessOrderbook(null, update);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("onClose: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("onError: " + ex.getMessage());
    }

}
