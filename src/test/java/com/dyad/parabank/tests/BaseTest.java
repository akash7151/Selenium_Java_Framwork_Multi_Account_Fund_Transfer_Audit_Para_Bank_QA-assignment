package com.dyad.parabank.tests;

import com.dyad.parabank.config.ConfigReader;
import com.dyad.parabank.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverManager.getDriver();
        driver.manage().window().maximize();
        driver.get(ConfigReader.getBaseUrl() + "/index.htm");
        log.info("Navigated to ParaBank homepage");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
        log.info("Browser session closed");
    }
}
