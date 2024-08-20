@ignore
Feature: call winner api
  Scenario:
    Given url baseUrl
    And path 'graphql'
    And def season = winners[__loop].season
    And text query =
    """
    query FindWinner($season:Int!) {
      winner(season:$season){
        team
        points
        gf
        ga
        gd
      }
    }
    """
    And def variables = {"season": '#(season)'}
    And request { query: '#(query)', variables: '#(variables)' }
    When method POST
    Then status 200

