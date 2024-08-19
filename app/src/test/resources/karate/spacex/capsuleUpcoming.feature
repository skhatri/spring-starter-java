Feature: Spacex capsule Tests

  Background:
    * url spaceXBaseUrl
    * path '/capsules/upcoming'

  Scenario: Happy paths

    * def foo = 1234
    * def bar = 1234
    * match foo == bar

