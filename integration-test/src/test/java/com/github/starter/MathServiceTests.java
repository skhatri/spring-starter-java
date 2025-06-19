package com.github.starter;

import org.junit.jupiter.api.Tag;

import com.intuit.karate.junit5.Karate;

@Tag("integration")
public class MathServiceTests {
    
    @Karate.Test
    public Karate testMathCalculations() {
        return Karate.run("classpath:karate/math/calculations.feature");
    }
    
    @Karate.Test 
    public Karate testMathGraphQL() {
        return Karate.run("classpath:karate/math/graphql.feature");
    }
    
    @Karate.Test
    public Karate testMathErrorHandling() {
        return Karate.run("classpath:karate/math/error-handling.feature");
    }
} 