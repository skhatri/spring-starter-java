package com.github.starter.test.util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;
public class TestDataUtil {
    private static final Random RANDOM = new Random();
    private TestDataUtil() {
    }
    public static String randomString(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
    public static BigDecimal randomBigDecimal(double min, double max, int scale) {
        double randomValue = min + (max - min) * RANDOM.nextDouble();
        return BigDecimal.valueOf(randomValue).setScale(scale, RoundingMode.HALF_UP);
    }
    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }
}
