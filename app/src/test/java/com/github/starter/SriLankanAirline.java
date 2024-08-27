package com.github.starter;

import com.intuit.karate.junit5.Karate;

public class SriLankanAirline {

    @Karate.Test
    Karate airlineTests() {
        return Karate.run("classpath:karate/airline/srilankan.feature");
    }

    @Karate.Test
    Karate airlineTests_post() {
        return Karate.run("classpath:karate/airline/post.feature");
    }

}
