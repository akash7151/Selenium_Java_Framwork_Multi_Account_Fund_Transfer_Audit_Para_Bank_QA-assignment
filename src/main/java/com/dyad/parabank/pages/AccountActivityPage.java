package com.dyad.parabank.pages;

import com.dyad.parabank.model.TransactionRecord;
import com.dyad.parabank.utils.MoneyUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountActivityPage extends BasePage {

    private static final By PAGE_TITLE = By.cssSelector("#transactionTable + h1, h1.title");
    private static final By TRANSACTION_ROWS = By.cssSelector("#transactionTable tbody tr");
    private static final By ACTIVITY_PERIOD = By.id("month");
    private static final By ACTIVITY_TYPE = By.id("transactionType");
    private static final By APPLY_FILTER_BUTTON = By.cssSelector("input[value='Go']");

    public AccountActivityPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForVisible(By.id("transactionTable"));
    }

    public List<TransactionRecord> getAllTransactions() {
        waitForPage();
        showAllActivity();
        return parseVisibleTransactions();
    }

    private List<TransactionRecord> parseVisibleTransactions() {
        List<TransactionRecord> transactions = new ArrayList<>();
        int rowCount = driver.findElements(TRANSACTION_ROWS).size();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            WebElement row = driver.findElements(TRANSACTION_ROWS).get(rowIndex);
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 4) {
                continue;
            }
            String date = cells.get(0).getText().trim();
            String description = cells.get(1).getText().trim();
            String debitText = cells.get(2).getText().trim();
            String creditText = cells.get(3).getText().trim();

            if (date.isBlank() || description.isBlank()) {
                continue;
            }

            if (!debitText.isBlank() && debitText.contains("$")) {
                transactions.add(new TransactionRecord(
                        date,
                        description,
                        "Debit",
                        MoneyUtils.parseCurrency(debitText)
                ));
            }

            if (!creditText.isBlank() && creditText.contains("$")) {
                transactions.add(new TransactionRecord(
                        date,
                        description,
                        "Credit",
                        MoneyUtils.parseCurrency(creditText)
                ));
            }
        }
        return transactions;
    }

    private void showAllActivity() {
        if (!driver.findElements(ACTIVITY_PERIOD).isEmpty()) {
            new Select(waitForVisible(ACTIVITY_PERIOD)).selectByVisibleText("All");
        }
        if (!driver.findElements(ACTIVITY_TYPE).isEmpty()) {
            new Select(waitForVisible(ACTIVITY_TYPE)).selectByVisibleText("All");
        }
        if (!driver.findElements(APPLY_FILTER_BUTTON).isEmpty()) {
            click(APPLY_FILTER_BUTTON);
        }
        waitForVisible(By.id("transactionTable"));
        wait.until(driver -> !driver.findElements(TRANSACTION_ROWS).isEmpty());
    }

    public void waitForTransaction(String transactionType, BigDecimal amount) {
        wait.until(driver -> parseVisibleTransactions().stream()
                .anyMatch(tx -> tx.getType().equalsIgnoreCase(transactionType)
                        && tx.getAmount().compareTo(amount) == 0));
    }

    public long countTransactionsMatching(String descriptionContains, double amount) {
        return getAllTransactions().stream()
                .filter(tx -> tx.getDescription().contains(descriptionContains))
                .filter(tx -> tx.getAmount().compareTo(MoneyUtils.toAmount(amount)) == 0)
                .count();
    }
}
