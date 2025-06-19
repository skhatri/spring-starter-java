package com.github.starter;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;
public class Conditions {
    public static boolean ignoreMock() {
        return false;
    }
    public static @NotNull Boolean checkEnv(String envName) {
        return Optional.ofNullable(System.getenv(envName)).map(Boolean::valueOf).orElse(false);
    }
    public static boolean ignoreBoot() {
        return false;
    }
}
