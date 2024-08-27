Feature: EPL Listing

  Background:
    * url baseUrl

  Scenario: List biggest margin in EPL
    * path '/epl/biggest-margin'
    * method GET
    * status 200
    * print response
    * match response[0].team == "Manchester Utd"
    * match response[0].season == "#number"

  Scenario: List most goal in EPL
    * path '/epl/most-goals'
    * method GET
    * status 200


  Scenario: List EPL season performance
    * path '/epl/season-performance'
    * param team = 'Manchester Utd'
    * method GET
    * status 200
    * print response


  Scenario: List EPL season table
      * path '/epl/season-table'
      * param season = '2021'
      * method GET
      * status 200


  Scenario: List EPL winners
      * path '/epl/winners'
      * method GET
      * status 200


  Scenario: EPL biggest margin, season 2020
      * path '/epl/biggest-margin'
      * param season = '2021'
      * method GET
      * status 200


  Scenario:  Season 2018 Manchester City has scored 106 goal and conceded 27 playing 38 games and won Premier League
      * path '/epl/winners'
      * method GET
      * status 200
      * def result = karate.filter(response, function(x){return x.season == '2018'})
      * match result[0].team == 'Manchester City'
      * match result[0].gf == 106
      * match result[0].ga == 27
      * match result[0].played == 38
      * match result[0].ranking == 1

  Scenario: Manchester United had won Premier League in 2003 and had a total points of 83
      * path '/epl/winners'
      * method GET
      * status 200
      * def teamWon = karate.filter(response, function(x){return x.team == 'Manchester Utd' && x.season == '2003'})
      * match teamWon[0].points == 83
      * match teamWon[0].ranking ==1

  Scenario: List All Premier League winner between 2012-2017 & 2011 Manchester Utd has won the Premier League
      * path '/epl/winners'
      * method GET
      * status 200
      * def champions = karate.filter(response, function(x){return x.season >= 2012 && x.season <= 2017})
      * def team_won_2011 = karate.filter(response, function(x){return x.season == '2011'})
      * match team_won_2011[0].team == 'Manchester Utd'
      * match team_won_2011[0].ranking == 1
      * print response

  Scenario: List team scoring more than 100 goals then find high scoring team of 2010 confirming it's Chelsea
      * path '/epl/winners'
      * method GET
      * status 200
      * def high_scoring_team = karate.filter(response, function(x){return x.gf > 100})
      * def hst_2010 = karate.filter(response, function(x){return x.season == '2010'})
      * match hst_2010[0].team == 'Chelsea'
      * match hst_2010[0].team == '#string'
      * match hst_2010[0].gf == 103
      * match hst_2010[0].gd == 71
      * match hst_2010[0].gd == '#number'
      * assert hst_2010.length == 1
      * print high_scoring_team
      * print hst_2010







    #List of Endpoints => GET Request => Header => content-type = application/json
#  1. http://localhost:8080/epl/biggest-margin
#  2. http://localhost:8080/epl/most-goals
#  3. http://localhost:8080/epl/season-performance?team=Manchester%20Utd
#  4. http://localhost:8080/epl/season-table?season=2021
#  5. http://localhost:8080/epl/winners
#  6. http://localhost:8080/epl/all-teams
#  7. http://localhost:8080/epl/biggest-margin?season=2020
#  8. http://localhost:8080/epl/most-goals?season=2012
#  9. http://localhost:8080/epl/season-performance?team=Manchester%20Utd&season=2015 (team=liverpool,2018,2022)
# 10. http://localhost:8080/epl/season-table?season=anyYear
# 11. http://localhost:8080/epl/winners?season=anyYear
# 12. http://localhost:8080/epl/all-teams?season=anyYear
