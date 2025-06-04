package com.github.starter.test.util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
public class TestResourceUtil {
    private TestResourceUtil() {
    }
    public static String readResourceAsString(String resourcePath) {
        try {
            return new String(
                Objects.requireNonNull(
                    TestResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath)
                ).readAllBytes(),
                StandardCharsets.UTF_8
            );
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
    public static String readFileAsString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
