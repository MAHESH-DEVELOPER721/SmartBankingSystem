package com.banking.model;

import java.util.concurrent.atomic.AtomicLong;

public class Account {
    private static final AtomicLong count = new AtomicLong(1000);
    private String accountNumber;
    private double balance;

    public Account() {
        this.accountNumber = "ACCT" + count.getAndIncrement();
        this.balance = 5000.0; // Initial welcome bonus/balance
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    
    public synchronized void credit(double amount) {
        this.balance += amount;
    }
    
    public synchronized boolean debit(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
}
