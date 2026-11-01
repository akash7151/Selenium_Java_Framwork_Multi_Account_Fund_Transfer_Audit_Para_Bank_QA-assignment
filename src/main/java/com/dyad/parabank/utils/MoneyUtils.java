package com.dyad.parabank.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static BigDecimal parseCurrency(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Currency value cannot be blank");
        }
        String normalized = rawValue.replace("$", "").replace(",", "").trim();
        return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toAmount(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatCurrency(BigDecimal amount) {
        return String.format("$%,.2f", amount);
    }
}
