Feature: Premier League Standings

  Background:
    * url baseUrl + '/graphql'


  Scenario: Champions League positions
    Given text query =
      """
      {
        seasonTable(season: 2024) {
          team
          ranking
          gf
          ga
          gd
          points
        }
      }

      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * table positions
      | team              | ranking | gf | ga | gd | points |
      | 'Manchester City' | 1       | 96 | 34 | 62 | 91     |
      | 'Arsenal'         | 2       | 91 | 29 | 62 | 89     |
      | 'Liverpool'       | 3       | 86 | 41 | 45 | 82     |
      | 'Aston Villa'     | 4       | 76 | 61 | 15 | 68     |
    And def leaderboard = $.data.seasonTable
    And def top3 = leaderboard.slice(0,4)
    And def result = call read('position.feature') top3
    And match top3 == positions
