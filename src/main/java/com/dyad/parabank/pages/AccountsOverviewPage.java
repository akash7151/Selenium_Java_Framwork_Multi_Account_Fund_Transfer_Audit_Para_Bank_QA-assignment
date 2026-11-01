package com.dyad.parabank.pages;

import com.dyad.parabank.model.AccountSummary;
import com.dyad.parabank.utils.MoneyUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsOverviewPage extends BasePage {

    private static final By OVERVIEW_TABLE = By.id("accountTable");
    private static final By ACCOUNT_ROWS = By.cssSelector("#accountTable tbody tr");
    private static final By TOTAL_ROW = By.cssSelector("#accountTable tfoot tr td b");

    public AccountsOverviewPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForVisible(OVERVIEW_TABLE);
    }

    public void waitForFundingAccountsPresent() {
        waitForPage();
        wait.until(driver -> !getAccountSummariesInternal().isEmpty());
    }

    public List<AccountSummary> getAccountSummaries() {
        waitForFundingAccountsPresent();
        return getAccountSummariesInternal();
    }

    private List<AccountSummary> getAccountSummariesInternal() {
        List<AccountSummary> accounts = new ArrayList<>();
        List<WebElement> rows = driver.findElements(ACCOUNT_ROWS);

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 2) {
                continue;
            }
            List<WebElement> links = cells.get(0).findElements(By.tagName("a"));
            if (links.isEmpty()) {
                continue;
            }
            String accountId = links.get(0).getText().trim();
            String balanceText = cells.get(1).getText().trim();
            if (accountId.isBlank() || balanceText.isBlank()) {
                continue;
            }
            accounts.add(new AccountSummary(accountId, MoneyUtils.parseCurrency(balanceText)));
        }
        return accounts;
    }

    public Map<String, AccountSummary> getAccountsById() {
        Map<String, AccountSummary> accounts = new HashMap<>();
        for (AccountSummary summary : getAccountSummaries()) {
            accounts.put(summary.getAccountId(), summary);
        }
        return accounts;
    }

    public AccountSummary getAccountById(String accountId) {
        AccountSummary summary = getAccountsById().get(accountId);
        if (summary == null) {
            throw new IllegalArgumentException("Account not found on overview: " + accountId);
        }
        return summary;
    }

    public void openAccountDetails(String accountId) {
        click(By.linkText(accountId));
    }
}
