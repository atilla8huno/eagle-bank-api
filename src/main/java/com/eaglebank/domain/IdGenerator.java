package com.eaglebank.domain;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicInteger ACCOUNT_SEQ = new AtomicInteger(100_000);

    private IdGenerator() {
    }

    public static String nextUserId() {
        return "usr-" + randomAlphanumeric(8);
    }

    public static String nextTransactionId() {
        return "tan-" + randomAlphanumeric(8);
    }

    public static String nextAccountNumber() {
        int seq = ACCOUNT_SEQ.getAndUpdate(current -> current >= 999_999 ? 100_000 : current + 1);
        return String.format("01%06d", seq);
    }

    private static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
