package org.example;

public class Main {
    public static void main(String[] args) {
        BinanceLv2Digester digester = new BinanceLv2Digester("ETHUSDT");
        digester.start();
        new Thread(new OrderbookPrinter(digester)).start();
        System.out.println("main finished");
    }
}