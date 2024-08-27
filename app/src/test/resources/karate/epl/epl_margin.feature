Feature: EPL goal margin

    Background:
      * url baseUrl

Scenario: Manchester Utd has won against Ipswich Town at home  in 1995  scoring 9 goals

  * path '/epl/biggest-margin'
  * method GET
  * status 200
  * def team_won = karate.filter(response, function(x){return x.season == '1995'})
  * print team_won
  * match team_won[0].team == 'Manchester Utd'
  * match team_won[0].venue == 'H'
  * match team_won[0].opponent == 'Ipswich Town'
  * match team_won[0].gf == 9
  * assert team_won.length == 1


Scenario: Season 2020 Leicester City  played with Southampton winning with 9 away goals

  * path '/epl/biggest-margin'
  * method GET
  * status 200
  * def season_2020 = karate.filter(response, function(x){return x.season == '2020'})
  * print season_2020
  * match season_2020[0].team == 'Leicester City'
  * match season_2020[0].opponent == 'Southampton'
  * match season_2020[0].gf == 9
  * match season_2020[0].venue == 'A'
  * match season_2020[0].result == 'W'

  Scenario:  Manchester City in 1996 has ranked 18 with points 38 conceded 58 goals with goal difference of 25

    * path '/epl/season-performance'
    * param team = 'Manchester City'
    * method GET
    * status 200
    * def man_city = karate.filter(response, function(x){return x.season == '1996'})
    * print man_city
    * assert man_city.length == 1
    * match man_city[0].team == 'Manchester City'
    * match man_city[0].ga == 58
    * match man_city[0].gd == -25
