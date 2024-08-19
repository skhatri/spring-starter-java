package com.github.starter;

import com.intuit.karate.junit5.Karate;

class SpaceXKarateTests {

    @Karate.Test
    Karate capsuleTest() {
        return Karate.run("classpath:karate/spacex/capsule.feature");
    }

    @Karate.Test
    Karate capsuleC113test() {
        return Karate.run("classpath:karate/spacex/capsuleC113.feature");
    }

    @Karate.Test
    Karate capsuleUpcomingTest() {
        return Karate.run("classpath:karate/spacex/capsuleUpcoming.feature");
    }

    @Karate.Test
    Karate roadsterTest() {
        return Karate.run("classpath:karate/spacex/roadster.feature");
    }
}

