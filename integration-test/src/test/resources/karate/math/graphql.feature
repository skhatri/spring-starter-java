Feature: Math Service GraphQL API

  Background:
    * url mathBaseUrl + '/graphql'
    * configure retry = { count: 5, interval: 2000 }

  Scenario: Get server status via GraphQL
    Given request { "query": "{ status { status server_time } }" }
    When method POST
    Then status 200
    And match response.data.status.status == 'UP'
    And match response.data.status.server_time == '#string'

  Scenario: Calculate addition via GraphQL
    Given request { "query": "{ calculate(input: { operand1: 5.0, operand2: 3.0, operation: ADD }) { result operation operand1 operand2 expression } }" }
    When method POST
    Then status 200
    And match response.data.calculate.result == 8.0
    And match response.data.calculate.operation == 'ADD'
    And match response.data.calculate.operand1 == 5.0
    And match response.data.calculate.operand2 == 3.0
    And match response.data.calculate.expression == '#string'

  Scenario: Calculate multiplication via GraphQL
    Given request { "query": "{ calculate(input: { operand1: 7.0, operand2: 6.0, operation: MULTIPLY }) { result operation operand1 operand2 expression } }" }
    When method POST
    Then status 200
    And match response.data.calculate.result == 42.0
    And match response.data.calculate.operation == 'MULTIPLY'
    And match response.data.calculate.operand1 == 7.0
    And match response.data.calculate.operand2 == 6.0

  Scenario: Calculate division via GraphQL
    Given request { "query": "{ calculate(input: { operand1: 15.0, operand2: 3.0, operation: DIVIDE }) { result operation operand1 operand2 expression } }" }
    When method POST
    Then status 200
    And match response.data.calculate.result == 5.0
    And match response.data.calculate.operation == 'DIVIDE'

  Scenario: Calculate subtraction via GraphQL
    Given request { "query": "{ calculate(input: { operand1: 10.0, operand2: 4.0, operation: SUBTRACT }) { result operation operand1 operand2 expression } }" }
    When method POST
    Then status 200
    And match response.data.calculate.result == 6.0
    And match response.data.calculate.operation == 'SUBTRACT'

  Scenario: Get supported operations via GraphQL
    Given request { "query": "{ supportedOperations }" }
    When method POST
    Then status 200
    And match response.data.supportedOperations == ['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE']

  Scenario: Perform calculation with history via GraphQL mutation
    Given request { "query": "mutation { performCalculation(input: { operand1: 12.0, operand2: 4.0, operation: DIVIDE }) { id calculation { operand1 operand2 operation result expression } timestamp } }" }
    When method POST
    Then status 200
    And match response.data.performCalculation.id == '#string'
    And match response.data.performCalculation.calculation.result == 3.0
    And match response.data.performCalculation.calculation.operation == 'DIVIDE'
    And match response.data.performCalculation.timestamp == '#string'

  Scenario: Complex calculation via GraphQL
    Given request { "query": "{ first: calculate(input: { operand1: 2.0, operand2: 3.0, operation: MULTIPLY }) { result } second: calculate(input: { operand1: 10.0, operand2: 4.0, operation: SUBTRACT }) { result } }" }
    When method POST
    Then status 200
    And match response.data.first.result == 6.0
    And match response.data.second.result == 6.0

  Scenario: Health status subscription can be initiated
    Given request { "query": "subscription { streamHealth { id status server_time } }" }
    When method POST
    Then status 200
    # Note: Subscription over HTTP returns a reactive publisher descriptor
    And match response.data.upstreamPublisher.scanAvailable == true 