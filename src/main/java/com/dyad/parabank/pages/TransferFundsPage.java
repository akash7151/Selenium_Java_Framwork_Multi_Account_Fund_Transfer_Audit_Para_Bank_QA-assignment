package com.dyad.parabank.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class TransferFundsPage extends BasePage {

    private static final By AMOUNT = By.id("amount");
    private static final By FROM_ACCOUNT = By.id("fromAccountId");
    private static final By TO_ACCOUNT = By.id("toAccountId");
    private static final By TRANSFER_BUTTON = By.cssSelector("input[value='Transfer']");
    private static final By RESULT_TITLE = By.cssSelector("h1.title");
    private static final By ERROR_TITLE = By.cssSelector("h1.title.error");

    public TransferFundsPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForVisible(AMOUNT);
    }

    public void transferFunds(String fromAccountId, String toAccountId, double amount) {
        submitTransfer(fromAccountId, toAccountId, String.valueOf(amount));
    }

    public void submitTransfer(String fromAccountId, String toAccountId, String amountValue) {
        waitForPage();
        waitForAccountOption(FROM_ACCOUNT, fromAccountId);
        waitForAccountOption(TO_ACCOUNT, toAccountId);

        WebElement amountField = waitForVisible(AMOUNT);
        amountField.clear();
        if (amountValue != null && !amountValue.isBlank()) {
            amountField.sendKeys(amountValue);
        }

        Select fromSelect = new Select(waitForVisible(FROM_ACCOUNT));
        fromSelect.selectByValue(fromAccountId);

        Select toSelect = new Select(waitForVisible(TO_ACCOUNT));
        toSelect.selectByValue(toAccountId);

        click(TRANSFER_BUTTON);
        waitForTransferResponse();
    }

    public void waitForTransferResponse() {
        try {
            wait.until(driver -> {
                String panelText = driver.findElement(By.id("rightPanel")).getText();
                return panelText.contains("Transfer Complete")
                        || panelText.contains("Error")
                        || panelText.toLowerCase().contains("insufficient");
            });
        } catch (org.openqa.selenium.TimeoutException ex) {
            // Invalid submissions may keep the user on the transfer form without a result panel.
        }
    }

    public boolean isStillOnTransferForm() {
        return isDisplayed(AMOUNT) && isDisplayed(TRANSFER_BUTTON);
    }

    public String resolveTransferAccount(String preferredAccountId, By dropdown) {
        waitForAccountDropdownLoaded(dropdown);
        Select select = new Select(waitForVisible(dropdown));
        List<String> available = select.getOptions().stream()
                .map(option -> option.getAttribute("value"))
                .filter(value -> value != null && !value.isBlank())
                .toList();
        if (available.contains(preferredAccountId)) {
            return preferredAccountId;
        }
        if (available.isEmpty()) {
            throw new IllegalStateException("No accounts available in transfer dropdown");
        }
        return available.get(0);
    }

    private void waitForAccountDropdownLoaded(By locator) {
        wait.until(driver -> hasSelectableAccounts(locator));
    }

    private void waitForAccountOption(By locator, String accountId) {
        wait.until(driver -> getAccountIds(locator).contains(accountId));
    }

    private List<String> getAccountIds(By locator) {
        Select select = new Select(driver.findElement(locator));
        return select.getOptions().stream()
                .map(option -> option.getAttribute("value"))
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private boolean hasSelectableAccounts(By locator) {
        return !getAccountIds(locator).isEmpty();
    }

    public boolean isTransferComplete() {
        String panelText = driver.findElement(By.id("rightPanel")).getText();
        return panelText.contains("Transfer Complete!");
    }

    public String getTransferResultMessage() {
        try {
            return waitForVisible(By.id("rightPanel")).getText();
        } catch (Exception ex) {
            return driver.findElement(By.tagName("body")).getText();
        }
    }

    public boolean isErrorDisplayed() {
        return driver.findElements(ERROR_TITLE).stream().anyMatch(WebElement::isDisplayed);
    }

    public String getErrorMessage() {
        if (!isErrorDisplayed()) {
            return "";
        }
        return getText(ERROR_TITLE);
    }
}
