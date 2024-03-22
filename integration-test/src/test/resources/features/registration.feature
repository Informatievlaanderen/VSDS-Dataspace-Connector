Feature: Registration

  Scenario Data Provider successfully registers as a dataspace participant
  A Data Provider wants to register as a dataspace participant
    Given I have installed the Provider Connector
    And a Dataspace Authority is available
    And I comply to the requirements for being register as a dataspace participant

    When I initiate the dataspace enrolment
    And the Dataspace Authority accepts my registration request

    Then the Dataspace Authority signs my credentials
    And the Dataspace Authority returns the signed credentials

  Scenario Data Provider is not able to register as a dataspace participant
  A Data Provider wants to register as a dataspace participant, but does not meet the requirements

    Given I have installed the Provider Connector
    And a Dataspace Authority is available
    And I do not comply to the requirements for being registered as a dataspace participant

    When I initiate the dataspace enrolment
    And the Dataspace Authority refuses my registration request

    Then the Dataspace Authority does not sign my credentials
    And the Dataspace Authority sends an error message

  Scenario Data Consumer successfully registers as a dataspace participant
  A Data Provider wants to register as a dataspace participant

    Given I have installed the Consumer Connector
    And a Dataspace Authority is available
    And I comply to the requirements for being registered as a dataspace participant

    When I initiate the dataspace enrolment
    And the Dataspace Authority accepts my registration request

    Then the Dataspace Authority signs my credentials
    And the Dataspace Authority returns the signed credentials

  Scenario Data Consumer  is not able to register as a dataspace participant
  A Data Provider wants to register as a dataspace participant, but does not meet the requirements

    Given I have installed the Consumer Connector
    And a Dataspace Authority is available
    And I do not comply to the requirements for being registered as a dataspace participant

    When I initiate the dataspace enrolment
    And the Dataspace Authority refuses my registration request

    Then the Dataspace Authority does not sign my credentials
    And the Dataspace Authority sends an error message