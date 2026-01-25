package com.banking.service;

import com.banking.model.Transaction;
import java.util.*;
import java.util.stream.Collectors;

public class FraudDetectionService {
    // DSA 1: Graph for Money Laundering Cycle Detection (Adjacency List)
    private Map<String, List<String>> transactionGraph = new HashMap<>();
    
    // DSA 2: PriorityQueue for Risk Scoring (Targeting High Risk)
    private PriorityQueue<Transaction> riskQueue = new PriorityQueue<>();
    
    // DSA 3: Sliding Window for Velocity Checks
    private Map<String, List<Long>> userTransactionHistory = new HashMap<>();

    public double evaluateRisk(Transaction t) {
        double score = 0.0;
        
        // 1. Large Amount Check
        if (t.getAmount() > 10000) {
            score += 0.4;
        }
        
        // 2. Velocity Check (> 5 transactions in 1 minute)
        List<Long> times = userTransactionHistory.getOrDefault(t.getSourceUser(), new ArrayList<>());
        times.add(t.getTimestamp());
        times.removeIf(time -> time < t.getTimestamp() - 60000); // 60 sec window
        if (times.size() > 5) {
            score += 0.5;
        }
        userTransactionHistory.put(t.getSourceUser(), times);
        
        // 3. Cycle Detection (Money Laundering Loop: A -> B -> C -> A)
        updateGraph(t.getSourceUser(), t.getDestUser());
        if (detectCycle(t.getSourceUser())) {
            score += 0.8; // High Risk
        }
        
        t.setRiskScore(score);
        if (score > 0.0) {
            riskQueue.add(t);
        }
        
        return score;
    }
    
    private void updateGraph(String from, String to) {
        transactionGraph.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }
    
    private boolean detectCycle(String startNode) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        return dfs(startNode, visited, recursionStack);
    }
    
    private boolean dfs(String node, Set<String> visited, Set<String> recursionStack) {
        visited.add(node);
        recursionStack.add(node);
        
        if (transactionGraph.containsKey(node)) {
            for (String neighbor : transactionGraph.get(node)) {
                if (!visited.contains(neighbor)) {
                    if (dfs(neighbor, visited, recursionStack)) return true;
                } else if (recursionStack.contains(neighbor)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(node);
        return false;
    }
    
    public List<Transaction> getRiskLog() {
        return riskQueue.stream()
                .sorted() // PriorityQueue iterator is not sorted, so we verify sort here
                .collect(Collectors.toList());
    }
}
