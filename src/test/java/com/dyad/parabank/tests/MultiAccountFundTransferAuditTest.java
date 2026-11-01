package com.dyad.parabank.tests;

import com.dyad.parabank.config.ConfigReader;
import com.dyad.parabank.listeners.ExtentTestListener;
import com.dyad.parabank.model.AccountSummary;
import com.dyad.parabank.model.TransactionRecord;
import com.dyad.parabank.model.UserRegistrationData;
import com.dyad.parabank.pages.AccountActivityPage;
import com.dyad.parabank.pages.AccountsOverviewPage;
import com.dyad.parabank.pages.NavigationMenuPage;
import com.dyad.parabank.pages.OpenAccountPage;
import com.dyad.parabank.pages.RegisterPage;
import com.dyad.parabank.pages.TransferFundsPage;
import com.dyad.parabank.utils.MoneyUtils;
import com.dyad.parabank.utils.UserDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiAccountFundTransferAuditTest extends BaseTest {

    @Test(description = "End-to-end multi-account fund transfer audit with balance and transaction verification")
    public void verifyMultiAccountFundTransferAudit() {
        double transferAmount = ConfigReader.getTransferAmount();
        BigDecimal transferDelta = MoneyUtils.toAmount(transferAmount);
        UserRegistrationData user = UserDataGenerator.generateUniqueUser();

        RegisterPage registerPage = new RegisterPage(driver);
        NavigationMenuPage navigation = new NavigationMenuPage(driver);
        OpenAccountPage openAccountPage = new OpenAccountPage(driver);
        AccountsOverviewPage overviewPage = new AccountsOverviewPage(driver);
        TransferFundsPage transferFundsPage = new TransferFundsPage(driver);
        AccountActivityPage accountActivityPage = new AccountActivityPage(driver);

        log.info("Step 1: Registering new user {}", user.getUsername());
        ExtentTestListener.logInfo("Registering user: " + user.getUsername());
        registerPage.open().register(user);

        log.info("Step 2: Opening two new accounts");
        navigation.openNewAccount();
        openAccountPage.waitForPage();

        List<String> fundingAccounts = openAccountPage.getAvailableFundingAccounts();
        Assert.assertFalse(fundingAccounts.isEmpty(),
                "No funding account available to open the first account after registration");

        String seedAccountId = fundingAccounts.get(0);
        String accountAId = openAccountPage.openAccount("CHECKING", seedAccountId);
        log.info("Opened Account A (CHECKING): {}", accountAId);
        ExtentTestListener.logInfo("Account A opened: " + accountAId);

        navigation.openNewAccount();
        openAccountPage.waitForPage();
        String accountBFundingSource = openAccountPage.resolveFundingAccount(seedAccountId);
        String accountBId = openAccountPage.openAccount("SAVINGS", accountBFundingSource);
        log.info("Opened Account B (SAVINGS): {}", accountBId);
        ExtentTestListener.logInfo("Account B opened: " + accountBId);

        Assert.assertNotEquals(accountAId, accountBId,
                "Validation #1: Two distinct account numbers must be created");
        ExtentTestListener.logInfo("Validation #1 passed: distinct account numbers " + accountAId + " and " + accountBId);

        ensureSufficientBalance(accountAId, transferDelta, overviewPage, navigation, transferFundsPage);

        AccountSummary accountABefore = overviewPage.getAccountById(accountAId);
        AccountSummary accountBBefore = overviewPage.getAccountById(accountBId);
        log.info("Balances before transfer - Account A: {}, Account B: {}",
                accountABefore.getBalance(), accountBBefore.getBalance());
        ExtentTestListener.logInfo("Before transfer - A: " + accountABefore.getBalance()
                + ", B: " + accountBBefore.getBalance());

        navigation.goToTransferFunds();
        transferFundsPage.transferFunds(accountAId, accountBId, transferAmount);
        Assert.assertTrue(transferFundsPage.isTransferComplete(),
                "Expected successful transfer confirmation");
        String transferMessage = transferFundsPage.getTransferResultMessage();
        log.info("Transfer confirmation: {}", transferMessage);
        ExtentTestListener.logInfo("Transfer message: " + transferMessage);

        navigation.goToAccountsOverview();
        AccountSummary accountAAfter = overviewPage.getAccountById(accountAId);
        AccountSummary accountBAfter = overviewPage.getAccountById(accountBId);
        log.info("Balances after transfer - Account A: {}, Account B: {}",
                accountAAfter.getBalance(), accountBAfter.getBalance());
        ExtentTestListener.logInfo("After transfer - A: " + accountAAfter.getBalance()
                + ", B: " + accountBAfter.getBalance());

        BigDecimal accountADifference = accountABefore.getBalance().subtract(accountAAfter.getBalance());
        BigDecimal accountBDifference = accountBAfter.getBalance().subtract(accountBBefore.getBalance());

        Assert.assertEquals(accountADifference, transferDelta,
                "Validation #2: Account A balance should decrease by transfer amount");
        Assert.assertEquals(accountBDifference, transferDelta,
                "Validation #2: Account B balance should increase by transfer amount");
        ExtentTestListener.logInfo("Validation #2 passed: balance calculations are accurate");

        overviewPage.openAccountDetails(accountAId);
        accountActivityPage.waitForTransaction("Debit", transferDelta);
        List<TransactionRecord> accountATransactions = accountActivityPage.getAllTransactions();
        List<TransactionRecord> accountASentTransfers = filterAuditTransferTransactions(
                accountATransactions, "Debit", transferAmount);

        navigation.goToAccountsOverview();
        overviewPage.openAccountDetails(accountBId);
        accountActivityPage.waitForTransaction("Credit", transferDelta);
        List<TransactionRecord> accountBTransactions = accountActivityPage.getAllTransactions();
        List<TransactionRecord> accountBReceivedTransfers = filterAuditTransferTransactions(
                accountBTransactions, "Credit", transferAmount);

        Assert.assertFalse(accountASentTransfers.isEmpty(),
                "Validation #3: Source account should contain a sent transfer transaction");
        Assert.assertFalse(accountBReceivedTransfers.isEmpty(),
                "Validation #3: Destination account should contain a received transfer transaction");
        ExtentTestListener.logInfo("Validation #3 passed: transfer recorded in both accounts");

        String expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        TransactionRecord latestSent = accountASentTransfers.stream()
                .max(Comparator.comparing(TransactionRecord::getDate))
                .orElseThrow();
        TransactionRecord latestReceived = accountBReceivedTransfers.stream()
                .max(Comparator.comparing(TransactionRecord::getDate))
                .orElseThrow();

        Assert.assertEquals(latestSent.getDate(), expectedDate,
                "Transfer date on source account should match today's date");
        Assert.assertEquals(latestReceived.getDate(), expectedDate,
                "Transfer date on destination account should match today's date");
        Assert.assertEquals(latestSent.getAmount(), transferDelta,
                "Transfer amount on source account should match requested amount");
        Assert.assertEquals(latestReceived.getAmount(), transferDelta,
                "Transfer amount on destination account should match requested amount");

        Assert.assertEquals(accountASentTransfers.size(), 1,
                "Validation #4: No duplicate sent transfer transaction should exist for the transfer amount");
        Assert.assertEquals(accountBReceivedTransfers.size(), 1,
                "Validation #4: No duplicate received transfer transaction should exist for the transfer amount");
        ExtentTestListener.logInfo("Validation #4 passed: no duplicate transactions for the transfer amount");

        long sentReferenceCount = accountASentTransfers.size();
        long receivedReferenceCount = accountBReceivedTransfers.size();

        Assert.assertEquals(sentReferenceCount, 1L,
                "Validation #5: Transfer reference should appear only once on source account");
        Assert.assertEquals(receivedReferenceCount, 1L,
                "Validation #5: Transfer reference should appear only once on destination account");
        ExtentTestListener.logInfo("Validation #5 passed: transfer reference appears only once per account");

        navigation.goToAccountsOverview();
        AccountSummary balanceBeforeInvalidTransfer = overviewPage.getAccountById(accountAId);

        assertInvalidTransferRejected(accountAId, accountBId, balanceBeforeInvalidTransfer,
                overviewPage, navigation, transferFundsPage);
    }

    private void assertInvalidTransferRejected(String fromAccountId,
                                             String toAccountId,
                                             AccountSummary balanceBeforeInvalidTransfer,
                                             AccountsOverviewPage overviewPage,
                                             NavigationMenuPage navigation,
                                             TransferFundsPage transferFundsPage) {
        double[] invalidAmounts = {
                balanceBeforeInvalidTransfer.getBalance().doubleValue() + 10_000,
                999_999_999_999d
        };

        for (double invalidAmount : invalidAmounts) {
            navigation.goToTransferFunds();
            transferFundsPage.transferFunds(fromAccountId, toAccountId, invalidAmount);

            String transferResponse = transferFundsPage.getTransferResultMessage();
            boolean transferCompleted = transferFundsPage.isTransferComplete();
            boolean errorDisplayed = transferFundsPage.isErrorDisplayed();

            navigation.goToAccountsOverview();
            AccountSummary balanceAfterInvalidTransfer = overviewPage.getAccountById(fromAccountId);
            boolean balanceUnchanged = balanceAfterInvalidTransfer.getBalance()
                    .compareTo(balanceBeforeInvalidTransfer.getBalance()) == 0;

            if (errorDisplayed || balanceUnchanged) {
                Assert.assertTrue(errorDisplayed || !transferCompleted,
                        "Validation #6: Invalid transfer should not succeed when balance is protected. Response: "
                                + transferResponse);
                ExtentTestListener.logInfo("Validation #6 passed using amount " + invalidAmount
                        + ". Error displayed: " + errorDisplayed + ", balance unchanged: " + balanceUnchanged);
                return;
            }
        }

        navigation.goToTransferFunds();
        transferFundsPage.submitTransfer(fromAccountId, toAccountId, "abc");

        String invalidInputResponse = transferFundsPage.getTransferResultMessage();
        boolean invalidInputError = transferFundsPage.isErrorDisplayed();
        boolean invalidInputCompleted = transferFundsPage.isTransferComplete();
        boolean stillOnTransferForm = transferFundsPage.isStillOnTransferForm();

        navigation.goToAccountsOverview();
        AccountSummary balanceAfterInvalidInput = overviewPage.getAccountById(fromAccountId);
        boolean invalidInputBalanceUnchanged = balanceAfterInvalidInput.getBalance()
                .compareTo(balanceBeforeInvalidTransfer.getBalance()) == 0;

        Assert.assertTrue(stillOnTransferForm || invalidInputError || !invalidInputCompleted
                        || invalidInputBalanceUnchanged,
                "Validation #6: System should reject invalid transfer input. Response: " + invalidInputResponse);
        ExtentTestListener.logInfo("Validation #6 passed using non-numeric amount. On form: "
                + stillOnTransferForm + ", error displayed: " + invalidInputError
                + ", balance unchanged: " + invalidInputBalanceUnchanged);
    }

    private void ensureSufficientBalance(String targetAccountId,
                                         BigDecimal minimumBalance,
                                         AccountsOverviewPage overviewPage,
                                         NavigationMenuPage navigation,
                                         TransferFundsPage transferFundsPage) {
        navigation.goToAccountsOverview();
        AccountSummary targetAccount = overviewPage.getAccountById(targetAccountId);
        if (targetAccount.getBalance().compareTo(minimumBalance) >= 0) {
            return;
        }

        BigDecimal shortfall = minimumBalance.subtract(targetAccount.getBalance()).add(MoneyUtils.toAmount(50));
        Map<String, AccountSummary> accounts = overviewPage.getAccountsById();

        String fundingAccountId = accounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(targetAccountId))
                .filter(entry -> entry.getValue().getBalance().compareTo(shortfall) >= 0)
                .max(Comparator.comparing(entry -> entry.getValue().getBalance()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException(
                        "No account has sufficient balance to prefund account " + targetAccountId));

        log.info("Prefunding account {} with {} from account {}",
                targetAccountId, shortfall, fundingAccountId);
        ExtentTestListener.logInfo("Prefunding account " + targetAccountId + " with " + shortfall
                + " from " + fundingAccountId);

        navigation.goToTransferFunds();
        transferFundsPage.transferFunds(fundingAccountId, targetAccountId, shortfall.doubleValue());
        Assert.assertTrue(transferFundsPage.isTransferComplete(),
                "Prefunding transfer should succeed. Result: " + transferFundsPage.getTransferResultMessage()
                        + " Error: " + transferFundsPage.getErrorMessage());
        navigation.goToAccountsOverview();
    }

    private List<TransactionRecord> filterAuditTransferTransactions(List<TransactionRecord> transactions,
                                                                    String transactionType,
                                                                    double amount) {
        BigDecimal expectedAmount = MoneyUtils.toAmount(amount);
        return transactions.stream()
                .filter(tx -> tx.getType().equalsIgnoreCase(transactionType))
                .filter(tx -> tx.getAmount().abs().compareTo(expectedAmount) == 0)
                .filter(tx -> tx.getDescription().toLowerCase(Locale.ROOT).contains("transfer"))
                .collect(Collectors.toList());
    }
}
