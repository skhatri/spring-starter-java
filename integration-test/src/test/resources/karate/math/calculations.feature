Feature: Math Service REST API Calculations

  Background:
    * url mathBaseUrl + '/calculate'
    * configure retry = { count: 5, interval: 2000 }

  Scenario: Addition calculation
    Given request { "number1": 5, "number2": 3, "operation": "ADD" }
    When method POST
    Then status 200
    And match response.result == 8.0
    And match response.operands.number1 == 5.0
    And match response.operands.number2 == 3.0
    And match response.operation == 'ADD'
    And match response.timestamp == '#string'

  Scenario: Subtraction calculation
    Given request { "number1": 10, "number2": 4, "operation": "SUBTRACT" }
    When method POST
    Then status 200
    And match response.result == 6.0
    And match response.operands.number1 == 10.0
    And match response.operands.number2 == 4.0
    And match response.operation == 'SUBTRACT'
    And match response.timestamp == '#string'

  Scenario: Multiplication calculation
    Given request { "number1": 7, "number2": 6, "operation": "MULTIPLY" }
    When method POST
    Then status 200
    And match response.result == 42.0
    And match response.operands.number1 == 7.0
    And match response.operands.number2 == 6.0
    And match response.operation == 'MULTIPLY'
    And match response.timestamp == '#string'

  Scenario: Division calculation
    Given request { "number1": 15, "number2": 3, "operation": "DIVIDE" }
    When method POST
    Then status 200
    And match response.result == 5.0
    And match response.operands.number1 == 15.0
    And match response.operands.number2 == 3.0
    And match response.operation == 'DIVIDE'
    And match response.timestamp == '#string'

  Scenario: Decimal division calculation
    Given request { "number1": 22, "number2": 7, "operation": "DIVIDE" }
    When method POST
    Then status 200
    And match response.result == 3.142857142857143
    And match response.result == '#number'
    And match response.operation == 'DIVIDE'
    And match response.timestamp == '#string'

  Scenario: Large number calculations
    Given request { "number1": 9999, "number2": 1, "operation": "ADD" }
    When method POST
    Then status 200
    And match response.result == 10000.0
    And match response.operands.number1 == 9999.0
    And match response.operands.number2 == 1.0
    And match response.operation == 'ADD'
    And match response.timestamp == '#string'

  Scenario: Negative number addition
    Given request { "number1": -5, "number2": 3, "operation": "ADD" }
    When method POST
    Then status 200
    And match response.result == -2.0
    And match response.operands.number1 == -5.0
    And match response.operands.number2 == 3.0
    And match response.operation == 'ADD'

  Scenario: Negative number subtraction
    Given request { "number1": 5, "number2": -3, "operation": "SUBTRACT" }
    When method POST
    Then status 200
    And match response.result == 8.0
    And match response.operands.number1 == 5.0
    And match response.operands.number2 == -3.0
    And match response.operation == 'SUBTRACT'

  Scenario: Zero operand calculations
    Given request { "number1": 0, "number2": 5, "operation": "ADD" }
    When method POST
    Then status 200
    And match response.result == 5.0
    And match response.operands.number1 == 0.0
    And match response.operands.number2 == 5.0
    And match response.operation == 'ADD'

  Scenario: Zero operand multiplication
    Given request { "number1": 0, "number2": 100, "operation": "MULTIPLY" }
    When method POST
    Then status 200
    And match response.result == 0.0
    And match response.operands.number1 == 0.0
    And match response.operands.number2 == 100.0
    And match response.operation == 'MULTIPLY'
    And match response.timestamp == '#string' 