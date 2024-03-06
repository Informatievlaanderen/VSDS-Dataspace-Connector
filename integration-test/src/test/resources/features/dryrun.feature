Feature: Existing full blown VSDS E2E test

  Scenario: Setup of a Provider - Connector setup with Authority
    Given I have a Provider Connector instance "provider"
    And I have a Consumer Connector instance "consumer"
    And I have a Federated Authority instance "authority"
    And I have an LDES Server set up
    And Authority contains a did entry for "consumer"
    When I ingest 50 members in the LDES Server
    And I register "consumer" to the Authority "authority"

