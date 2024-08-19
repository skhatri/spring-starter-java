Feature: Spacex capsule Tests

  Background:
    * url spaceXBaseUrl
    * path '/capsules/C113'

  Scenario: Details of C204 is "Currently in construction for use in DM-2"
    * method GET
    * status 200
    * def capsule_C204 = karate.filter(response, function(x){return x.capsule_serial = 'C204'})
    * def C204_details = karate.filter(response, function(x){return x.details =='Currently in construction for use in DM-2'})



