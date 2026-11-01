package com.dyad.parabank.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class NavigationMenuPage extends BasePage {

    private static final By OPEN_NEW_ACCOUNT_LINK = By.linkText("Open New Account");
    private static final By ACCOUNTS_OVERVIEW_LINK = By.linkText("Accounts Overview");
    private static final By TRANSFER_FUNDS_LINK = By.linkText("Transfer Funds");

    public NavigationMenuPage(WebDriver driver) {
        super(driver);
    }

    public void openNewAccount() {
        click(OPEN_NEW_ACCOUNT_LINK);
    }

    public void goToAccountsOverview() {
        click(ACCOUNTS_OVERVIEW_LINK);
    }

    public void goToTransferFunds() {
        click(TRANSFER_FUNDS_LINK);
    }
}
