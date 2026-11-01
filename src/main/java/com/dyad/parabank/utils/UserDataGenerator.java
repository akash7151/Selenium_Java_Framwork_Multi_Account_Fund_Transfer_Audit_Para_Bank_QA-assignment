package com.dyad.parabank.utils;

import com.dyad.parabank.model.UserRegistrationData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class UserDataGenerator {

    private UserDataGenerator() {
    }

    public static UserRegistrationData generateUniqueUser() {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100, 999);

        return new UserRegistrationData(
                "Test",
                "User" + suffix,
                "123 Automation Street",
                "Boston",
                "MA",
                "02110",
                "555-" + suffix.substring(Math.max(0, suffix.length() - 7)),
                generateSsn(),
                "user_" + suffix,
                "Pass@" + suffix
        );
    }

    private static String generateSsn() {
        int part1 = ThreadLocalRandom.current().nextInt(100, 999);
        int part2 = ThreadLocalRandom.current().nextInt(10, 99);
        int part3 = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("%03d-%02d-%04d", part1, part2, part3);
    }
}
