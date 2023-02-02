package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        BinanceLv2Digester digester = new BinanceLv2Digester(logger, "ETHUSDT");
        digester.start();
        new Thread(new OrderbookPrinter(digester, 10, 10000)).start();
        System.out.println("main finished");
    }
}