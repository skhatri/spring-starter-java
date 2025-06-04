Feature: Pokemon Graphql Listing

  Background:
    * url baseUrl + '/graphql'

  Scenario: Bulbasaur is the first item in the paginated list
    Given text query =
      """
      {
        getPokemonList{
          content {
            name
            primaryType
            total 
            secondaryType
            pokedex
            region
          }
          hasNext
          hasPrevious
          totalCount
        }
      }

      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * def pokePage = $.data.getPokemonList
    * def pokeList = pokePage.content
    * match pokeList[0].name == "Bulbasaur"
    * match pokePage.hasNext == true
    * match pokePage.hasPrevious == false
    
    
  Scenario: Get Pokemon by name includes region
    Given text query =
      """
      {
        pokemon(name: "Pikachu") {
          name
          primaryType
          total
          secondaryType
          pokedex
          region
        }
      }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    * def pokemon = $.data.pokemon
    * match pokemon.name == "Pikachu"
    * match pokemon.region != null
