package com.banking;

import com.banking.server.GlobalHttpServer;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Smart Banking System...");
            GlobalHttpServer.start();
            System.out.println("System is running. Open index.html in your browser.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
