Feature: Pokemon Listing

  Background:
    * url baseUrl
    * path '/pokemon/list'

  Scenario: Pikachu is the first

    * method GET
    * status 200
    * def firstPokemon = $[0]
    * match firstPokemon.name == "pikachu"
    * match firstPokemon.baseStat == 320



  Scenario: Arceus primary type is normal
    * method GET
    * status 200
    # karate.filter is a function. first argument is body and second is criteria to filter
    * def arceus = karate.filter(response, function(x){ return x.name == 'arceus' })
    * assert arceus.length == 1
    * match arceus[0].primaryType == 'normal'

  Scenario: The only dark pokemon name is chien-pao
    * method GET
    * status 200
    * def dark_pokemon = karate.filter(response, function(x) { return x.primaryType == 'dark'})
    * assert dark_pokemon.length > 0

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
