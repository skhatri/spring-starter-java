Feature: Pokemon Listing

  Background:
    * url baseUrl
    * path '/pokemon/list'

  Scenario: Pikachu is the first

    * method GET
    * status 200
    * match $[0].name == "pikachu"

  Scenario: Arceus primary type is normal

    * method GET
    * status 200
    * def arceus = karate.filter(response, function(x){ return x.name == 'arceus' })
    * assert arceus.length == 1
    * match arceus[0].primaryType == 'normal'

Scenario: Koraidon secondary type is dragon and has bunch of weaknesses

    * method GET
    * status 200
    * def filteredList = karate.filter(response, function(x){ return x.name == 'koraidon' })
    * assert filteredList.length == 1
    * def koraidon = filteredList[0]
    * match koraidon.secondaryType == 'dragon'
    * assert koraidon.weakness.length == 5
    * def expectedWeakness = karate.sort(["fairy", "ice", "dragon", "psychic", "flying"])
    * def actualWeakness = karate.sort(karate.jsonPath(koraidon, '$.weakness'))
    * match expectedWeakness == actualWeakness
