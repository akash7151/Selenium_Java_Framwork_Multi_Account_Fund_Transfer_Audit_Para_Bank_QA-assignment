package com.dyad.parabank.model;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountSummary {

    private final String accountId;
    private final BigDecimal balance;

    public AccountSummary(String accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountSummary that)) {
            return false;
        }
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "AccountSummary{accountId='" + accountId + "', balance=" + balance + '}';
    }
}
