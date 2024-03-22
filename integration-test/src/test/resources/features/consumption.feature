Feature: Consumption
  Scenario Data Consumer who is part of the dataspace successfully consumes data from a Data Provider

    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am registered as a Dataspace Participant
    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider verifies my identity and participation in the dataspace
    And the Data Provider offers me a contract regarding the dataset I want to consume
    And I accept the contract offered by the Data Provider
    Then I am able to consume the dataset


  Scenario Data Consumer who is part of the dataspace is denied consumption by a Data Provider

    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am registered as a Dataspace Participant
    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider verifies my identity and participation in the dataspace
    And the Data Provider denies access to the dataset (for which reason? Does there need to be reason? :slight_smile: )
    Then I am not able to consume the dataset

  Scenario Data Consumer who is not part of the dataspace is denied consumption by a Data Provider
    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am not registered as a Dataspace Participant
    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider cannot verify my identity and participation in the dataspace
    And the Data Provider denies access to the dataset
    Then I am not able to consume the dataset

  Scenario Data Consumer who is part of the dataspace does not accept the contract, no counteroffer is done by the Data Consumer

    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am registered as a Dataspace Participant

    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider verifies my identity and participation in the dataspace
    And the Data Provider offers me a contract regarding the dataset I want to consume
    And I decline the contract

    Then I am not able to consume the dataset


  Scenario Data Consumer who is part of the dataspace does not accept the contract, does a counteroffer which is not accepted
  Data Consumer who is part of the dataspace does not accept the contract, does a counteroffer which is not accepted. No data consumption is done

    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am registered as a Dataspace Participant

    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider verifies my identity and participation in the dataspace
    And the Data Provider offers me a contract regarding the dataset I want to consume
    And I decline the contract and make a counteroffer
    And the Data Provider does not accept the counteroffer

    Then I am not able to consume the dataset


  Scenario Data Consumer who is part of the dataspace does not accept the contract, does a counteroffer which is accepted
  Data Consumer who is part of the dataspace does not accept the contract, does a counteroffer which is accepted. Data consumption is done

    Given I have installed the Consumer Connector
    And a Data Provider offers a dataset which I’m interested in
    And I am registered as a Dataspace Participant

    When I contact the Data Provider to consume the dataset I’m interested in
    And the Data Provider verifies my identity and participation in the dataspace
    And the Data Provider offers me a contract regarding the dataset I want to consume
    And I decline the contract and make a counteroffer
    And the Data Provider accepts the counteroffer

    Then I am able to consume the dataset