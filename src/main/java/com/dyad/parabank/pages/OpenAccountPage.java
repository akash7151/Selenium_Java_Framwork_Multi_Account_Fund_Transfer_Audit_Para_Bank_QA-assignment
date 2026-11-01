package com.dyad.parabank.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAccountPage extends BasePage {

    private static final By ACCOUNT_TYPE = By.id("type");
    private static final By FROM_ACCOUNT = By.id("fromAccountId");
    private static final By OPEN_ACCOUNT_BUTTON = By.cssSelector("input[value='Open New Account']");
    private static final By CONFIRMATION_TITLE = By.cssSelector("#openAccountResult h1.title");
    private static final By CONFIRMATION_MESSAGE = By.id("openAccountResult");

    private static final Pattern ACCOUNT_NUMBER_PATTERN =
            Pattern.compile("account number:?\\s*#?(\\d+)", Pattern.CASE_INSENSITIVE);

    public OpenAccountPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForVisible(ACCOUNT_TYPE);
    }

    public List<String> getAvailableFundingAccounts() {
        waitForFundingAccountsLoaded();
        Select select = new Select(waitForVisible(FROM_ACCOUNT));
        List<String> accountIds = new ArrayList<>();
        for (WebElement option : select.getOptions()) {
            String value = option.getAttribute("value");
            if (value != null && !value.isBlank()) {
                accountIds.add(value.trim());
            }
        }
        return accountIds;
    }

    public void waitForFundingAccountsLoaded() {
        wait.until(driver -> {
            Select select = new Select(driver.findElement(FROM_ACCOUNT));
            return select.getOptions().stream()
                    .map(option -> option.getAttribute("value"))
                    .anyMatch(value -> value != null && !value.isBlank());
        });
    }

    public void waitForFundingAccountOption(String accountId) {
        wait.until(driver -> getAvailableFundingAccounts().contains(accountId));
    }

    public String resolveFundingAccount(String preferredAccountId) {
        List<String> available = getAvailableFundingAccounts();
        if (available.contains(preferredAccountId)) {
            return preferredAccountId;
        }
        if (available.isEmpty()) {
            throw new IllegalStateException("No funding accounts available in dropdown");
        }
        return available.get(0);
    }

    public String openAccount(String accountType, String fundFromAccountId) {
        waitForFundingAccountOption(fundFromAccountId);

        Select typeSelect = new Select(waitForVisible(ACCOUNT_TYPE));
        typeSelect.selectByVisibleText(accountType);

        Select fromSelect = new Select(waitForVisible(FROM_ACCOUNT));
        fromSelect.selectByValue(fundFromAccountId);

        click(OPEN_ACCOUNT_BUTTON);
        waitForVisible(CONFIRMATION_TITLE);

        String confirmationText = getText(CONFIRMATION_MESSAGE);
        Matcher matcher = ACCOUNT_NUMBER_PATTERN.matcher(confirmationText);
        if (!matcher.find()) {
            throw new IllegalStateException("Unable to parse new account number from: " + confirmationText);
        }
        return matcher.group(1);
    }
}
