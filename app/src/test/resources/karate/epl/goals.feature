Feature: Goals Scored in a Match

  Background:
    * url baseUrl + '/graphql'


  Scenario: Most Goals scored by a team in different years
    Given text query =
      """
      {
        mostGoals(season: 2021){
          team
          opponent
          gf
          ga
          matchDate
        }
      }

      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * table expected
      | team             | opponent      | gf | ga | matchDate                       |
      | 'Manchester Utd' | 'Southampton' | 9  | 0  | 'Tue Feb 02 00:00:00 AEDT 2021' |
    And def topGoals = $.data.mostGoals
    And match topGoals == expected

  Scenario: Biggest goal margin in season 2012
    Given text query =
      """
      {
        biggestMargin(season: 2012){
          team
          opponent
          gf
          ga
          matchDate
        }
      }

      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * table expected
      | team             | opponent    | gf | ga | matchDate                       |
      | 'Manchester Utd' | 'Arsenal'   | 8  | 2  | 'Sun Aug 28 00:00:00 AEST 2011' |
      | 'Fulham'         | 'QPR'       | 6  | 0  | 'Sat Oct 01 00:00:00 AEST 2011' |
      | 'Arsenal'        | 'Blackburn' | 7  | 1  | 'Sat Feb 04 00:00:00 AEDT 2012' |

    And def biggestMargin = $.data.biggestMargin
    And match biggestMargin == expected