Feature: Airline status check



Scenario: Sri Lankan Airways List
  * url 'https://api.instantwebtools.net/v1/airlines'
  * method GET
  * status 200
  * def sri_LankanAirline = karate.filter(response, function(x){return x.name == 'Sri Lankan Airways'})
  * def particular_id = karate.filter(response, function(x){return x._id == "987f52fa-9195-4b5a-8109-e0b62b388882"})
  * print particular_id
  * match particular_id[0].head_quaters == "Katunayake, Sri Lanka"

Scenario: Get Airline data using ID
  * url 'https://api.instantwebtools.net/v1/airlines'
  * path '/8197a60d-0bcf-4cf3-bb4b-405fd46f020c'
  * method GET
  * match response.name == 'PIA'
  * print response
  * status 200
  * print responseStatus
  * print responseTime
  * print responseCookies


Scenario: Get API Demo

  * url 'https://reqres.in/api/users?page=2'
  * method GET
  * status 200
  * print response.data
  * print response.data[0]
  * match response.data[0].email == 'michael.lawson@reqres.in'
  * print response.data[0].email
  * match response.per_page == 6
  * match response.data[1].last_name == 'Ferguson'
  * match response.data[5].avatar == 'https://reqres.in/img/faces/12-image.jpg'

Scenario: Post request demo


