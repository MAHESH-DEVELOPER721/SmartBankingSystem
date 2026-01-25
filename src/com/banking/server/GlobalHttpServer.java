package com.banking.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;
import com.banking.model.*;
import com.banking.service.FraudDetectionService;

public class GlobalHttpServer {
    private static Map<String, User> users = new HashMap<>(); 
    private static Map<String, List<Transaction>> history = new HashMap<>(); // User -> Transactions
    private static FraudDetectionService fraudService = new FraudDetectionService();

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/details", new DetailsHandler());
        server.createContext("/api/transfer", new TransferHandler());
        server.createContext("/api/fraud-alerts", new FraudAlertsHandler());
        server.createContext("/", new StaticHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    // CORS & Response Helper
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim().replace("{", "").replace("}", "");
        if (json.isEmpty()) return map;
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] entry = pair.split(":");
            if (entry.length == 2) {
                String key = entry[0].trim().replace("\"", "");
                String value = entry[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String json = br.lines().collect(Collectors.joining());
                Map<String, String> data = parseJson(json);
                
                String username = data.get("username");
                String password = data.get("password");

                if (users.containsKey(username)) {
                    sendResponse(exchange, 400, "{\"message\": \"User already exists\"}");
                } else {
                    User newUser = new User(username, password);
                    users.put(username, newUser);
                    history.put(username, new ArrayList<>());
                    sendResponse(exchange, 201, "{\"message\": \"User created\"}");
                }
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String json = br.lines().collect(Collectors.joining());
                Map<String, String> data = parseJson(json);
                
                String username = data.get("username");
                String password = data.get("password");
                
                User user = users.get(username);
                if (user != null && user.checkPassword(password)) {
                    sendResponse(exchange, 200, "{\"message\": \"Login successful\", \"username\": \"" + username + "\"}");
                } else {
                    sendResponse(exchange, 401, "{\"message\": \"Invalid credentials\"}");
                }
            }
        }
    }

    static class DetailsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String username = query.split("=")[1]; // simple parse
            
            User user = users.get(username);
            if (user != null) {
                List<Transaction> txs = history.get(username);
                // Construc JSON manually
                StringBuilder txJson = new StringBuilder("[");
                for (int i = 0; i < txs.size(); i++) {
                     Transaction t = txs.get(i);
                     txJson.append(String.format("{\"id\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"amount\":%.2f,\"risk\":%.2f}", 
                         t.getId(), t.getSourceUser(), t.getDestUser(), t.getAmount(), t.getRiskScore()));
                     if (i < txs.size() - 1) txJson.append(",");
                }
                txJson.append("]");

                String json = String.format("{\"username\":\"%s\",\"accountNumber\":\"%s\",\"balance\":%.2f, \"history\":%s}", 
                    user.getUsername(), user.getAccount().getAccountNumber(), user.getAccount().getBalance(), txJson.toString());
                sendResponse(exchange, 200, json);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"User not found\"}");
            }
        }
    }

    static class TransferHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String json = br.lines().collect(Collectors.joining());
                Map<String, String> data = parseJson(json);
                
                String from = data.get("from");
                String to = data.get("to");
                double amount = Double.parseDouble(data.get("amount"));

                User sender = users.get(from);
                User receiver = users.get(to);

                if (sender != null && receiver != null) {
                    if (sender.getAccount().debit(amount)) {
                        receiver.getAccount().credit(amount);
                        
                        Transaction t = new Transaction(UUID.randomUUID().toString(), from, to, amount);
                        // Fraud Check
                        double risk = fraudService.evaluateRisk(t);
                        
                        history.get(from).add(t);
                        history.get(to).add(t); // show in both
                        
                        String msg = risk > 0.5 ? "Transfer successful but flagged for review!" : "Transfer successful";
                        sendResponse(exchange, 200, "{\"message\": \"" + msg + "\", \"risk\": " + risk + "}");
                    } else {
                        sendResponse(exchange, 400, "{\"message\": \"Insufficient funds\"}");
                    }
                } else {
                    sendResponse(exchange, 404, "{\"message\": \"User not found\"}");
                }
            }
        }
    }

    static class FraudAlertsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            List<Transaction> risky = fraudService.getRiskLog();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < risky.size(); i++) {
                Transaction t = risky.get(i);
                sb.append(String.format("{\"id\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"amount\":%.2f,\"risk\":%.2f}", 
                     t.getId(), t.getSourceUser(), t.getDestUser(), t.getAmount(), t.getRiskScore()));
                if (i < risky.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString());
        }
    }

    static class StaticHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            File file = new File("public" + path);
            if (file.exists()) {
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                if (path.endsWith(".js")) contentType = "application/javascript";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                OutputStream os = exchange.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
            } else {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
