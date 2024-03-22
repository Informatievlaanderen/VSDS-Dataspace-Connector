Feature: EDC Configuration

  Scenario A Data Provider determines a contract for an asset
  A Data Provider determines a contract for an asset in the asset catalogue
    Given I have installed the Provider Connector
    When a LDES is available in my catalogue
    Then I can configure a custom contract for that LDES

  Scenario A Data Provider determines a policy for an asset
  A Data Provider determines a policy for an asset in the asset catalogue
    Given I have installed the Provider Connector
    When a LDES is available in my catalogue
    Then I can configure a policy for that LDES

  Scenario Data Provider defines a new view, which is created as an asset in the Connector
    Given I have installed the Provider Connector
    When a new view is created on the LDES Server linked to the Connector
    Then I can see the new view in the catalogue of the Connector
    And the new view is offered as an asset through the Connector

  Scenario Data Provider removes a view, which is removed as an asset in the Connector
    Given I have installed the Provider Connector
    And at least one view is published in the catalogue on the Connector
    When an existing view is deleted on the LDES Server linked to the Connector
    Then the deleted view is removed from the catalogue of the Connector
    And Consumers consuming the view will not be able to continue consuming the view