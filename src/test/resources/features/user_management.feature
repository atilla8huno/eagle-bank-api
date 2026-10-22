Feature: Customer Profile Management

  A customer can register with Eagle Bank and view their own profile.
  A customer may not view another customer's profile.

  Scenario: A visitor can register as a customer
    When a visitor registers as customer "Alice"
    Then "Alice" can view the profile of "Alice"

  Scenario: A visitor cannot register without required details
    When a visitor attempts to register without required details
    Then the profile request is rejected with "Registration was rejected because required details were missing"

  Scenario: A registered customer can view their own profile
    Given a registered customer "Alice"
    Then "Alice" can view the profile of "Alice"

  Scenario: A customer cannot view another customer's profile
    Given a registered customer "Alice"
    And a registered customer "Bob"
    When "Alice" attempts to view the profile of "Bob"
    Then the profile request is rejected with "Customer 'Alice' is not allowed to access profile of 'Bob'"

  Scenario: A customer cannot view a profile that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to view the profile of "UnknownCustomer"
    Then the profile request is rejected with "Customer profile 'UnknownCustomer' was not found"

  @wip
  Scenario: A customer can update their own profile
    Given a registered customer "Alice"
    When "Alice" updates the profile of "Alice"
    Then the profile of "Alice" is updated

  @wip
  Scenario: A customer cannot update another customer's profile
    Given a registered customer "Alice"
    And a registered customer "Bob"
    When "Alice" attempts to update the profile of "Bob"
    Then the profile update is rejected

  @wip
  Scenario: A customer cannot update a profile that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to update a profile that does not exist
    Then the profile is not found

  @wip
  Scenario: A customer with no bank account can delete their own profile
    Given a registered customer "Alice"
    When "Alice" deletes the profile of "Alice"
    Then the profile of "Alice" is no longer available

  @wip
  Scenario: A customer with an active bank account cannot delete their profile
    Given a registered customer "Alice"
    And "Alice" has opened a "personal" bank account "EverydayAccount"
    When "Alice" attempts to delete the profile of "Alice"
    Then the profile request is rejected with "Customer 'Alice' cannot be deleted while associated with a bank account"

  @wip
  Scenario: A customer cannot delete another customer's profile
    Given a registered customer "Alice"
    And a registered customer "Bob"
    When "Alice" attempts to delete the profile of "Bob"
    Then the profile deletion is rejected

  @wip
  Scenario: A customer cannot delete a profile that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to delete a profile that does not exist
    Then the profile is not found
