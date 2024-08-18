Feature: Pokemon Graphql Listing

  Background:
    * url baseUrl + '/graphql'

  Scenario: Pikachu is the first item in the list
    Given text query =
      """
      {
        getPokemonList{
          name
          primaryType
          baseStat
          secondaryType
          weight
        }
      }

      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * def pokeList = $.data.getPokemonList
    * match pokeList[0].name == "pikachu"
