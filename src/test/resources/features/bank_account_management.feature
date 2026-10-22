Feature: Bank Account Management

  An authenticated customer can open personal bank accounts, view their
  bank accounts, and fetch their bank account details. Customers are
  prevented from viewing bank accounts owned by other customers.

  Scenario: A customer opens a personal bank account
    Given a registered customer "Alice"
    When "Alice" opens a "personal" bank account "EverydayAccount"
    Then "Alice" sees a balance of "0.00" "GBP" in "EverydayAccount"

  Scenario: A customer cannot open a bank account without required details
    Given a registered customer "Alice"
    When "Alice" attempts to open a bank account without required details
    Then the account access is rejected with "Opening bank account was rejected because required details were missing"

  Scenario: A customer views their bank accounts
    Given a registered customer "Alice"
    And "Alice" opens a "personal" bank account "EverydayAccount"
    Then "EverydayAccount" is listed for "Alice"

  Scenario: A customer views their bank account details
    Given a registered customer "Alice"
    And "Alice" opens a "personal" bank account "EverydayAccount"
    Then "Alice" sees a balance of "0.00" "GBP" in "EverydayAccount"

  Scenario: A customer cannot view another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" opens a "personal" bank account "AliceSavings"
    When "Bob" attempts to view "AliceSavings"
    Then the account access is rejected with "Customer 'Bob' is not allowed to access bank account 'AliceSavings'"

  Scenario: A customer cannot view a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to view "UnknownAccount"
    Then the account access is rejected with "Bank account 'UnknownAccount' was not found"

  @wip
  Scenario: A customer can update their own bank account
    Given a registered customer "Alice"
    And "Alice" opens a "personal" bank account "EverydayAccount"
    When "Alice" updates "EverydayAccount"
    Then "EverydayAccount" is updated for "Alice"

  @wip
  Scenario: A customer cannot update another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" opens a "personal" bank account "AliceSavings"
    When "Bob" attempts to update "AliceSavings"
    Then the account update is rejected

  @wip
  Scenario: A customer cannot update a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to update a bank account that does not exist
    Then the bank account is not found

  @wip
  Scenario: A customer can delete their own bank account
    Given a registered customer "Alice"
    And "Alice" opens a "personal" bank account "EverydayAccount"
    When "Alice" deletes "EverydayAccount"
    Then "EverydayAccount" is no longer listed for "Alice"

  @wip
  Scenario: A customer cannot delete another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" opens a "personal" bank account "AliceSavings"
    When "Bob" attempts to delete "AliceSavings"
    Then the account deletion is rejected

  @wip
  Scenario: A customer cannot delete a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to delete a bank account that does not exist
    Then the bank account is not found
