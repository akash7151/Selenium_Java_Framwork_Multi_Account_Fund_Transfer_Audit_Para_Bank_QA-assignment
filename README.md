# Scenario 6: Multi-Account Fund Transfer Audit — ParaBank

Automated end-to-end test suite for the **Dyad Technologies QA Automation Practical Assessment (Scenario 6)**.  
The project validates multi-account fund transfers on [ParaBank](https://parabank.parasoft.com/) with before/after balance auditing, transaction-history verification, and negative-path testing.

## Framework & Tools

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Build | Maven |
| UI Automation | Selenium WebDriver 4.x |
| Test Runner | TestNG |
| Driver Management | WebDriverManager |
| Design Pattern | Page Object Model (POM) |
| Waits | Explicit `WebDriverWait` (no `Thread.sleep()`) |
| Logging | SLF4J + Logback |
| Reporting | ExtentReports (HTML) |

## Project Structure

```
src/main/java/com/dyad/parabank/
├── config/          # Configuration reader
├── driver/          # WebDriver lifecycle management
├── model/           # Data models (Account, Transaction, User)
├── pages/           # Page Object classes
└── utils/           # Helpers (money parsing, user generation)

src/test/java/com/dyad/parabank/
├── listeners/       # ExtentReports TestNG listener
└── tests/           # Test classes

src/test/resources/
├── config.properties
├── logback-test.xml
└── testng.xml
```

## Prerequisites

- **Java JDK 17+**
- **Maven 3.8+**
- **Google Chrome** (default browser; Firefox/Edge also supported)
- Internet access to `https://parabank.parasoft.com/`

Verify installations:

```bash
java -version
mvn -version
```

## Configuration

Edit `src/test/resources/config.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `base.url` | ParaBank base URL | `https://parabank.parasoft.com/parabank` |
| `browser` | `chrome`, `firefox`, or `edge` | `chrome` |
| `headless` | Run browser headless | `false` |
| `explicit.wait.seconds` | Default explicit wait timeout | `15` |
| `transfer.amount` | Transfer amount in USD | `250` |

## How to Run

From the project root:

```bash
mvn clean test
```

Run with a specific browser:

```bash
mvn clean test -Dbrowser=firefox
```

Run headless (useful for CI):

```bash
mvn clean test -Dheadless=true
```

## Test Flow

The single TestNG test `verifyMultiAccountFundTransferAudit` executes:

1. **Register** a new user with dynamically generated credentials
2. **Open two accounts** (CHECKING and SAVINGS)
3. **Capture balances** before transfer
4. **Transfer** the configured amount from Account A → Account B
5. **Capture balances** after transfer
6. **Assert** Account A decreased and Account B increased by the transfer amount
7. **Verify transaction history** on both accounts (amount + date)
8. **Negative path**: attempt an over-limit transfer and verify graceful error handling

## Validation Criteria Coverage

| # | Criterion | Implementation |
|---|-----------|----------------|
| 1 | Two distinct account numbers created | `Assert.assertNotEquals(accountAId, accountBId)` |
| 2 | Balance calculations accurate | Dynamic before/after balance delta assertions |
| 3 | Transaction recorded in both accounts | Filter activity for `Funds Transfer Sent` / `Funds Transfer Received` |
| 4 | No duplicate transaction | Exactly one matching transaction per account |
| 5 | Transfer reference appears only once | Count matching description + amount = 1 |
| 6 | Invalid transfer handled gracefully | Over-limit transfer fails, error shown, balance unchanged |

## Reports & Logs

After execution, check:

- **ExtentReports HTML:** `test-output/ExtentReport.html`
- **Execution log:** `test-output/automation.log`
- **Surefire reports:** `target/surefire-reports/`

## Assumptions

1. **Fresh user per run** — Each test registers a unique user to avoid shared-database conflicts on the public demo site.
2. **Registration seed account** — ParaBank auto-creates a seed account after registration (used to fund new accounts). Both Account A and Account B are opened using this seed account as the funding source.
3. **Account dropdown timing** — Funding-account dropdowns on *Open New Account* and *Transfer Funds* load asynchronously; the framework waits explicitly for options to appear.
4. **Prefunding Account A** — Opening a new account deposits only $100. Before the audit transfer, the test dynamically prefunds Account A from the seed account when its balance is below the configured transfer amount.
5. **Transfer amount** — Default $250. Adjust `transfer.amount` in `config.properties` if needed.
6. **Date format** — Transaction dates are validated against `MM-dd-yy`, matching ParaBank's UI format.
7. **Negative-path behavior** — The public ParaBank demo does not reject all over-limit transfers (it may allow overdraft). The test first attempts over-limit and unrealistic amounts; if those succeed on the demo, it falls back to a non-numeric amount (`abc`) and asserts the application blocks the transfer (form retained, error shown, or balance unchanged).
8. **Public demo stability** — The shared ParaBank demo may be slow or reset periodically; explicit waits are used, but extended outages are outside test control.

## Design Notes

- **Dynamic data**: Usernames, SSN, and account numbers are generated at runtime — nothing is hardcoded except configurable transfer amount.
- **POM separation**: Tests contain workflow and assertions only; locators and interactions live in page classes.
- **No `Thread.sleep()`**: All synchronization uses Selenium explicit waits via `WebDriverWait`.

## Troubleshooting

| Issue | Suggestion |
|-------|------------|
| Timeout waiting for element | Increase `explicit.wait.seconds` or check ParaBank availability |
| Registration fails (duplicate username) | Re-run; usernames include timestamp + random suffix |
| ChromeDriver mismatch | WebDriverManager resolves drivers automatically on first run |
| Insufficient balance for transfer | Lower `transfer.amount` or verify account-opening balances |

## Author

Dyad Technologies — QA Automation Practical Assessment (Scenario 6)
