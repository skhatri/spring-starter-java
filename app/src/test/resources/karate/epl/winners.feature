Feature: Premier League Standings

  Background:
    * url baseUrl + '/graphql'


  Scenario: Champions League positions
    Given table winners
      | season | team              | gf  | ga | gd | points |
      | 2012   | 'Manchester City' | 93  | 29 | 64 | 91     |
      | 2013   | 'Manchester Utd'  | 86  | 43 | 43 | 89     |
      | 2014   | 'Manchester City' | 102 | 37 | 65 | 86     |
      | 2015   | 'Chelsea'         | 73  | 32 | 41 | 87     |
      | 2016   | 'Leicester City'  | 68  | 36 | 32 | 81     |
      | 2017   | 'Chelsea'         | 85  | 33 | 52 | 93     |
      | 2018   | 'Manchester City' | 106 | 27 | 79 | 100    |
      | 2019   | 'Manchester City' | 95  | 23 | 72 | 98     |
    * def collection = call read('call-winner.feature') winners
    * assert collection.length == 8
    * def winningTeams = $collection[*].response.data.winner[0].team
    * match winningTeams[*] == $winners[*].team
