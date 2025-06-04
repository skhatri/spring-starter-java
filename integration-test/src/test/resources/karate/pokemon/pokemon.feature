Feature: Pokemon Listing

Background:
    * url baseUrl
    * path '/pokemon/list'

Scenario: Bulbasaur is the first in paginated response

    * method GET
    * status 200
    * def pokeList = $.data
    * match pokeList[0].name == "Bulbasaur"
    * match $.pagination.hasNext == true
    * match $.pagination.hasPrevious == false


Scenario: Charmeleon primary type is fire in paginated response

    * method GET
    * status 200
    * def pokeList = $.data
    * def arceus = karate.filter(pokeList, function(x){ return x.name == 'Charmeleon' })
    * assert arceus.length == 1
    * match arceus[0].primaryType == 'Fire'

Scenario: Pidgeot secondary type is Flying in paginated response

    * method GET
    * status 200
    * def pokeList = $.data
    * def filteredList = karate.filter(pokeList, function(x){ return x.name == 'Pidgeot' })
    * assert filteredList.length == 1
    * def pidgeot = filteredList[0]
    * match pidgeot.secondaryType == 'Flying'

Scenario: Pokemon list without effectiveness parameter should not include effectiveness

    * method GET
    * status 200
    * def pokeList = $.data
    * match pokeList[0].effectiveness == null
    
Scenario: Pokemon list with effectiveness=true should include effectiveness data

    * param effectiveness = true
    * method GET
    * status 200
    * def pokeList = $.data
    * match pokeList[0].effectiveness != null
    * match pokeList[0].effectiveness.noEffect != null

