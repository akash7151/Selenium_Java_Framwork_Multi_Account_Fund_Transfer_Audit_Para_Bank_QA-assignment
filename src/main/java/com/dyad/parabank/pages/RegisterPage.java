package com.dyad.parabank.pages;

import com.dyad.parabank.model.UserRegistrationData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {

    private static final By FIRST_NAME = By.name("customer.firstName");
    private static final By LAST_NAME = By.name("customer.lastName");
    private static final By ADDRESS = By.name("customer.address.street");
    private static final By CITY = By.name("customer.address.city");
    private static final By STATE = By.name("customer.address.state");
    private static final By ZIP_CODE = By.name("customer.address.zipCode");
    private static final By PHONE = By.name("customer.phoneNumber");
    private static final By SSN = By.name("customer.ssn");
    private static final By USERNAME = By.name("customer.username");
    private static final By PASSWORD = By.name("customer.password");
    private static final By CONFIRM_PASSWORD = By.name("repeatedPassword");
    private static final By REGISTER_BUTTON = By.cssSelector("input[value='Register']");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        navigateTo("register.htm");
        waitForVisible(FIRST_NAME);
        return this;
    }

    public void register(UserRegistrationData user) {
        type(FIRST_NAME, user.getFirstName());
        type(LAST_NAME, user.getLastName());
        type(ADDRESS, user.getAddress());
        type(CITY, user.getCity());
        type(STATE, user.getState());
        type(ZIP_CODE, user.getZipCode());
        type(PHONE, user.getPhone());
        type(SSN, user.getSsn());
        type(USERNAME, user.getUsername());
        type(PASSWORD, user.getPassword());
        type(CONFIRM_PASSWORD, user.getPassword());
        click(REGISTER_BUTTON);
    }
}
