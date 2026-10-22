Feature: Bank Account Transactions

  An authenticated customer can deposit funds into and withdraw funds from
  their own bank account, list their transactions, and fetch individual
  transaction details.

  Scenario: A customer deposits money into their bank account
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    When "Alice" deposits "250.00" "GBP" into "MainAccount" as "SalaryDeposit"
    Then "Alice" sees a balance of "250.00" "GBP" in "MainAccount"
    And a "deposit" of "250.00" is recorded on "MainAccount" for "Alice"

  Scenario: A customer withdraws money from their bank account
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    And "Alice" deposits "250.00" "GBP" into "MainAccount" as "InitialDeposit"
    When "Alice" withdraws "75.00" "GBP" from "MainAccount" as "AtmWithdrawal"
    Then "Alice" sees a balance of "175.00" "GBP" in "MainAccount"
    And a "withdrawal" of "75.00" is recorded on "MainAccount" for "Alice"

  Scenario: A customer cannot withdraw more than their available balance
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    And "Alice" deposits "100.00" "GBP" into "MainAccount" as "InitialDeposit"
    When "Alice" attempts to withdraw "250.00" "GBP" from "MainAccount"
    Then the transaction is rejected with "Insufficient funds in 'MainAccount' to process withdrawal of 250.00"
    And "Alice" sees a balance of "100.00" "GBP" in "MainAccount"

  Scenario: A customer cannot deposit or withdraw on another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" has a "personal" bank account "AliceVault"
    And "Alice" deposits "500.00" "GBP" into "AliceVault" as "InitialDeposit"
    When "Bob" attempts to withdraw "50.00" "GBP" from "AliceVault"
    Then the transaction is rejected with "Customer 'Bob' is not allowed to transact on 'AliceVault'"

  Scenario: A customer views all transactions on their bank account
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    And "Alice" deposits "200.00" "GBP" into "MainAccount" as "FirstDeposit"
    And "Alice" withdraws "50.00" "GBP" from "MainAccount" as "FirstWithdrawal"
    Then "Alice" sees 2 transactions listed on "MainAccount"

  Scenario: A customer cannot view transactions on another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" has a "personal" bank account "AliceVault"
    When "Bob" attempts to view transactions on "AliceVault"
    Then the transaction is rejected with "Customer 'Bob' is not allowed to access transactions of 'AliceVault'"

  Scenario: A customer fetches a transaction on their bank account
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    And "Alice" deposits "120.00" "GBP" into "MainAccount" as "BonusDeposit"
    Then "Alice" can fetch transaction "BonusDeposit" on "MainAccount" with type "deposit" and amount "120.00"

  Scenario: A customer cannot fetch a transaction on another customer's bank account
    Given a registered customer "Alice"
    And a registered customer "Bob"
    And "Alice" has a "personal" bank account "AliceVault"
    And "Alice" deposits "120.00" "GBP" into "AliceVault" as "VaultDeposit"
    When "Bob" attempts to fetch transaction "VaultDeposit" on "AliceVault"
    Then the transaction is rejected with "Customer 'Bob' is not allowed to access transaction 'VaultDeposit' on 'AliceVault'"

  Scenario: A customer cannot fetch a transaction that does not exist
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    When "Alice" attempts to fetch transaction "UnknownTransaction" on "MainAccount"
    Then the transaction is rejected with "Transaction 'UnknownTransaction' was not found on 'MainAccount'"

  Scenario: A customer cannot fetch a transaction against the wrong bank account
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    And "Alice" has a "personal" bank account "OtherAccount"
    And "Alice" deposits "80.00" "GBP" into "MainAccount" as "MainDeposit"
    When "Alice" attempts to fetch transaction "MainDeposit" on "OtherAccount"
    Then the transaction is rejected with "Transaction 'MainDeposit' was not found on 'OtherAccount'"

  @wip
  Scenario: A customer cannot deposit or withdraw without required details
    Given a registered customer "Alice"
    And "Alice" has a "personal" bank account "MainAccount"
    When "Alice" attempts a transaction on "MainAccount" without required details
    Then the transaction is rejected because required details are missing

  @wip
  Scenario: A customer cannot deposit or withdraw on a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to deposit into a bank account that does not exist
    Then the bank account is not found

  @wip
  Scenario: A customer cannot list transactions on a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to list transactions on a bank account that does not exist
    Then the bank account is not found

  @wip
  Scenario: A customer cannot fetch a transaction on a bank account that does not exist
    Given a registered customer "Alice"
    When "Alice" attempts to fetch a transaction on a bank account that does not exist
    Then the bank account is not found
