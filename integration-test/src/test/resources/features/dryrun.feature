Feature: Existing full blown VSDS E2E test

  Scenario: Setup of a Provider - Connector setup with Authority
    Given I have a Provider Connector instance "provider"
    And I have a Consumer Connector instance "consumer"
    And I have a Federated Authority instance "authority"
    And I have an LDES Server set up
    And Authority contains a did entry for "consumer"
    And Authority contains a did entry for "provider"
    And I ingest 50 members in the LDES Server
    And I register "consumer" to the Authority "authority"
    And I have an LDIO pipeline setup to follow "consumer"
    When I create a HTTP dataplane in "provider"
    When I create a HTTP dataplane in "consumer"
    And I create an asset "devices" of the LDES Server in "provider"
    And I create a blank-cheque policy "blank-cheque" in "provider"
    And I create a contract "contract" with policy "blank-cheque" and access policy "blank-cheque" for all assets in "provider"
    When Participant "consumer" gets the policyId from "provider"
    And Participant "consumer" starts negotiating a contract with "provider" for asset "devices"
    Then I wait for "consumer"'s contract negotiation to finish
    And Participant "consumer" starts the transfer of "devices" in "provider"
    And the LDIO has started
