package com.banking.model;

public class Transaction implements Comparable<Transaction> {
    private String id;
    private String sourceUser;
    private String destUser;
    private double amount;
    private long timestamp;
    private double riskScore;

    public Transaction(String id, String source, String dest, double amount) {
        this.id = id;
        this.sourceUser = source;
        this.destUser = dest;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.riskScore = 0.0;
    }

    public String getId() { return id; }
    public String getSourceUser() { return sourceUser; }
    public String getDestUser() { return destUser; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double score) { this.riskScore = score; }

    @Override
    public int compareTo(Transaction other) {
        // Sort by risk score descending (Higher risk first)
        return Double.compare(other.riskScore, this.riskScore);
    }
}
