package com.github.starter;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Conditions {
    public static boolean ignoreMock() {
        return checkEnv("NO_WIREMOCK");
    }

    private static @NotNull Boolean checkEnv(String envName) {
        return Optional.ofNullable(System.getenv(envName)).map(Boolean::valueOf).orElse(false);
    }

    public static boolean ignoreBoot() {
        return checkEnv("NO_BOOT");
    }
}
