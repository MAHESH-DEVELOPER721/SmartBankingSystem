package com.banking.model;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class User {
    private String username;
    private String passwordHash;
    private Account account;

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.account = new Account();
    }

    public String getUsername() { return username; }
    public Account getAccount() { return account; }

    public boolean checkPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, encodedhash));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not supported");
        }
    }
}
