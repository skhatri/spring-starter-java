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
    * print response

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
     * print response

  Scenario: List EPL winners
      * path '/epl/winners'
      * method GET
      * status 200
      * print response

  Scenario: EPL biggest margin, season 2020
      * path '/epl/biggest-margin'
      * param season = '2021'
      * method GET
      * status 200
      * print response

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
