Feature: Post API demo


  Background:
    * url 'https://reqres.in/api'
    * def responseBody = read('response1.json')

  Scenario: Post Demo 1
    * path '/users'
    * request
    """
  {
  "name": "Samantha George",
  "job": "Data Analyst"
  }
    """
    * method POST
    * status 201
    * print response
    * match response == {"name": "Samantha George","job": "Data Analyst", "id":"#string","createdAt":"#ignore"}

  Scenario: Post Demo 2

    * path '/users'
    * def responseBody = read('response1.json')
    * def requestBody = read('request1.json')
    * request requestBody
    * set requestBody.name = 'Kader Khan'
    * method POST
    * print response
    * status 201
    * match response == responseBody
    * match response.id == '#notnull'


  Scenario:  Post Demo 3

    * path '/users'
    * def projectPath = karate.properties['user.dir']
    * print projectPath
    * def filepath = projectPath+'/src/main/Data/request2.json'
    * print filepath
    * def requestbody = filepath
    * request requestbody
    * method POST
    * status 201
    * print response






