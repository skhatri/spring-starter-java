Feature: Math Service Error Handling

  Background:
    * configure retry = { count: 3, interval: 1000 }

  Scenario: Division by zero error in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number1": 10.0, "number2": 0.0, "operation": "DIVIDE" }
    When method POST
    Then status 400

  Scenario: Invalid operation in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number1": 5.0, "number2": 3.0, "operation": "INVALID" }
    When method POST
    Then status 400

  Scenario: Missing operand in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number2": 3.0, "operation": "ADD" }
    When method POST
    Then status 200
    And match response.result == 3.0
    And match response.operands.number1 == 0.0
    And match response.operands.number2 == 3.0

  Scenario: Invalid JSON in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request 'invalid json'
    When method POST
    Then status 400

  Scenario: Division by zero error in GraphQL
    Given url mathBaseUrl
    And path '/graphql'
    And header Content-Type = 'application/json'
    And text query =
      """
      {
        calculate(input: {
          operand1: 15.0,
          operand2: 0.0,
          operation: DIVIDE
        }) {
          result
        }
      }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    And match response.errors == '#array'
    And match response.errors[0].message == '#string'

  Scenario: Invalid operation in GraphQL
    Given url mathBaseUrl
    And path '/graphql'
    And header Content-Type = 'application/json'
    And text query =
      """
      {
        calculate(input: {
          operand1: 5.0,
          operand2: 3.0,
          operation: INVALID_OPERATION
        }) {
          result
        }
      }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    And match response.errors == '#array'

  Scenario: Missing required field in GraphQL
    Given url mathBaseUrl
    And path '/graphql'
    And header Content-Type = 'application/json'
    And text query =
      """
      {
        calculate(input: {
          operand1: 5.0,
          operation: ADD
        }) {
          result
        }
      }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    And match response.errors == '#array'

  Scenario: Invalid GraphQL syntax
    Given url mathBaseUrl
    And path '/graphql'
    And header Content-Type = 'application/json'
    And request { "query": "{ calculate(input: { operand1: 5.0 operand2: 3.0 operation: ADD }) { result } }" }
    When method POST
    Then status 200
    And match response.data.calculate.result == 8.0

  Scenario: Very large numbers causing overflow in REST
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number1": 1.7976931348623157E+308, "number2": 2.0, "operation": "MULTIPLY" }
    When method POST
    Then status 200
    And match response.result == 'Infinity'

  Scenario: NaN handling in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number1": "NaN", "number2": 5.0, "operation": "ADD" }
    When method POST
    Then status 400

  Scenario: Infinity handling in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request { "number1": "Infinity", "number2": 5.0, "operation": "ADD" }
    When method POST
    Then status 400

  Scenario: Empty request body in REST API
    Given url mathBaseUrl
    And path '/calculate'
    And header Content-Type = 'application/json'
    And request {}
    When method POST
    Then status 400

  Scenario: Null operation in GraphQL mutation
    Given url mathBaseUrl
    And path '/graphql'
    And header Content-Type = 'application/json'
    And text query =
      """
      mutation {
        performCalculation(input: {
          operand1: 10.0,
          operand2: 5.0
        }) {
          calculation {
            result
          }
        }
      }
      """
    And request { query: '#(query)' }
    When method POST
    Then status 200
    And match response.errors == '#array' 