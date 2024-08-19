Feature: Spacex capsule Tests

  Background:
    * url spaceXBaseUrl
    * path '/capsules'

  Scenario: Basic Happy paths
    * method GET
    * status 200
    * def firstCapsule = $[0]
    * match firstCapsule.capsule_serial == "C101"

  Scenario:  Capsule C104 status is inactive
    * method GET
    * status 200
    * def capsule_C104 = karate.filter(response, function(x) {return x.capsule_serial = 'C104'})

  Scenario:  Capsule C202 status is active
    * method GET
    * status 200
    * def capsule_C202 = karate.filter(response, function(x) {return x.status = 'active'})







