Feature: English Premier League Graphql Listing

  Background:
    * url baseUrl + '/graphql'

  Scenario: Pikachu is the first item in the list
    Given text query =
      """
      {
        getEplList{
          season
          rankings
          team
          played
          gf
          ga
          gd
          points
        }
     }
    }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * def eplList = $.data.getEplList
    * match pokeList[0].name == "pikachu"