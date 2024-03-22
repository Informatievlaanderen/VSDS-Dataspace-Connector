Feature: The EDC Registration Service mentions offboarding and blacklisting as future operations

  Scenario Data Provider is leaving the dataspace
    Given I have installed the Provider Connector
    And a I am a registered Dataspace Participant
    When I want to leave the dataspace
    Then my credentials are revoked
    And I am removed from the dataspace participant list
    And I am not able to offer datasets onto the dataspace anymore

  Scenario Data Consumer is leaving the dataspace
    Given I have installed the Consumer Connector
    And a I am a registered Dataspace Participant
    When I want to leave the dataspace
    Then my credentials are revoked
    And I am removed from the dataspace participant list
    And I am not able to consume datasets from within the dataspace anymore

  Scenario Dataspace Authority revoking dataspace access from Dataspace Participant
    Given there is at least one registered and active Dataspace Participant
    When I revoke access to the dataspace for the Dataspace Participant
    Then the Dataspace Participant will not be able to offer or consume data within the dataspace