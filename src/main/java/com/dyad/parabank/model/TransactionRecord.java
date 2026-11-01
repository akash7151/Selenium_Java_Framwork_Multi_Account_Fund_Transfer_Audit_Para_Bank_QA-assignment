package com.dyad.parabank.model;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionRecord {

    private final String date;
    private final String description;
    private final String type;
    private final BigDecimal amount;

    public TransactionRecord(String date, String description, String type, BigDecimal amount) {
        this.date = date;
        this.description = description;
        this.type = type;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionRecord that)) {
            return false;
        }
        return Objects.equals(date, that.date)
                && Objects.equals(description, that.description)
                && Objects.equals(type, that.type)
                && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description, type, amount);
    }

    @Override
    public String toString() {
        return "TransactionRecord{date='" + date + "', description='" + description
                + "', type='" + type + "', amount=" + amount + '}';
    }
}
