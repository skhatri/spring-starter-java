package com.github.starter;

import com.intuit.karate.junit5.Karate;

public class KarateEplTests {

    @Karate.Test
    Karate eplTests() {
        return Karate.run("classpath:karate/epl/epl.feature");
    }

    @Karate.Test
    Karate eplTestsMargin() {
        return Karate.run("classpath:karate/epl/epl_margin.feature");
    }

}

