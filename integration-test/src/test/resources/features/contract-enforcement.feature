Feature: Contract Enforcement

  Scenario Data Provider checks conditions of use of a dataset a Data Consumer is consuming and does not find a breach
  A Data Provider checks the conditions of use of a dataset a Data Consumer is consuming. The usage complies to the conditions of use and the Data Consumer is permitted to keep on consuming the dataset

    Given I have installed the Provider Connector
    And a Data Consumer is consuming a dataset

    When I check the usage of the dataset against the conditions of use accepted by the Data Consumer
    And I see that the Data Consumer does not breaches the conditions of use

    Then the Data Consumer is able to keep consuming the dataset


  Scenario Data Provider checks conditions of use of a dataset a Data Consumer is consuming and finds a breach
  A Data Provider checks the conditions of use of a dataset a Data Consumer is consuming. The usage does not comply to the conditions of use and the Data Consumer is not permitted to keep on consuming the dataset

    Given I have installed the Provider Connector
    And a Data Consumer is consuming a dataset

    When I check the usage of the dataset against the conditions of use accepted by the Data Consumer
    And I see that the Data Consumer breaches the conditions of use

    Then I revoke the access of the Data Consumer to consume the dataset
    And the Data Consumer is not able to keep consuming the dataset