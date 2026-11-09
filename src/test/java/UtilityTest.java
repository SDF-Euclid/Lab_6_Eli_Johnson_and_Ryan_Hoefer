import accounts.Bill;
import accounts.UtilityUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

// This class was mostly gen rated by chatgpt but heavily changed by me(Ryan), also had it re write to include one assertion per test

public class UtilityTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Path utilityFile;
    private UtilityStore store;
    private UtilityUser utility;

    @Before
    public void setUp() throws IOException {
        utilityFile = temp.getRoot().toPath().resolve("utilities.json");
        store = new UtilityStore(utilityFile);
        utility = store.create("testUtility", "password");
    }

    @Test
    public void assignsSixDigitAccountNumber() {
        assertTrue(utility.getAccountNumber().matches("[1-9][0-9]{5}"));
    }

    @Test
    public void startsWithEmptyPaymentHistory() {
        assertTrue(utility.getPaidBills().isEmpty());
    }

    @Test
    public void setsFirstBillAmount() {
        assertEquals(100, utility.getNextBill().getAmount());
    }

    @Test
    public void setsFirstBillDueDate() {
        assertNotNull(utility.getNextBill().getDueDate());
    }

    @Test
    public void findsUtilityAccountByUsername() {
        assertSame(utility, store.find("testUtility"));
    }

    @Test
    public void findsUtilityAccountByAccountNumber() {
        assertSame(utility, store.find(utility.getAccountNumber()));
    }

    @Test
    public void returnsNullForUnknownUsername() {
        assertNull(store.find("unknown-user"));
    }

    @Test
    public void recordsOnePayment() {
        utility.payNextBill();

        assertEquals(1, utility.getPaidBills().size());
    }

    @Test
    public void movesOriginalBillToHistory() {
        Bill firstBill = utility.getNextBill();
        utility.payNextBill();

        assertSame(firstBill, utility.getPaidBills().get(0));
    }

    @Test
    public void marksOriginalBillPaid() {
        Bill firstBill = utility.getNextBill();
        utility.payNextBill();

        assertNotNull(firstBill.getPaidAt());
    }

    @Test
    public void advancesNextBillDueDate() {
        Bill firstBill = utility.getNextBill();
        utility.payNextBill();

        assertEquals(firstBill.getDueDate().plusMonths(1), utility.getNextBill().getDueDate());
    }

    @Test
    public void setsNextBillAmountAfterPayment() {
        utility.payNextBill();

        assertEquals(100, utility.getNextBill().getAmount());
    }

    @Test
    public void leavesNextBillUnpaid() {
        utility.payNextBill();

        assertNull(utility.getNextBill().getPaidAt());
    }

    @Test
    public void reloadsAccountAfterPayments() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertNotNull(saved);
    }

    @Test
    public void reloadsAccountUsername() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertEquals("testUtility", saved.getUsername());
    }

    @Test
    public void reloadsBothPaidBills() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertEquals(2, saved.getPaidBills().size());
    }

    @Test
    public void reloadsFirstPaymentTimestamp() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertNotNull(saved.getPaidBills().get(0).getPaidAt());
    }

    @Test
    public void reloadsSecondPaymentTimestamp() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertNotNull(saved.getPaidBills().get(1).getPaidAt());
    }

    @Test
    public void reloadsNextBillDueDate() throws IOException {
        UtilityUser saved = saveAndReloadAfterTwoPayments();

        assertEquals(utility.getNextBill().getDueDate(), saved.getNextBill().getDueDate());
    }

    private UtilityUser saveAndReloadAfterTwoPayments() throws IOException {
        utility.payNextBill();
        utility.payNextBill();
        store.save();

        return new UtilityStore(utilityFile).find(utility.getAccountNumber());
    }

    // Storage tests below were generated with ChatGPT.
    @Test
    public void rejectsNullStorage() throws IOException {
        Files.writeString(utilityFile, "null");

        assertThrows(IOException.class, () -> new UtilityStore(utilityFile));
    }

    @Test
    public void rejectsNullAmongValidAccounts() throws IOException {
        Files.writeString(utilityFile, """
                [
                  {"username":"first","password":"test","accountNumber":"123456",
                   "paidBills":[],"nextBill":{"amount":100,"dueDate":"2026-12-01"}},
                  null,
                  {"username":"second","password":"test","accountNumber":"654321",
                   "paidBills":[],"nextBill":{"amount":100,"dueDate":"2026-12-01"}}
                ]
                """);

        assertThrows(IOException.class, () -> new UtilityStore(utilityFile));
    }

    @Test
    public void rejectsSingleNullAccount() throws IOException {
        Files.writeString(utilityFile, "[null]");

        assertThrows(IOException.class, () -> new UtilityStore(utilityFile));
    }

    @Test
    public void rejectsIncompatibleAccountType() throws IOException {
        Files.writeString(utilityFile, "[42]");

        assertThrows(IOException.class, () -> new UtilityStore(utilityFile));
    }

    @Test
    public void acceptsEmptyAccountList() throws IOException {
        Files.writeString(utilityFile, "[]");
        UtilityStore reopened = new UtilityStore(utilityFile);

        assertNull(reopened.find(utility.getUsername()));
    }

    @Test
    public void canSaveAccountAfterLoadingEmptyList() throws IOException {
        Files.writeString(utilityFile, "[]");
        UtilityStore reopened = new UtilityStore(utilityFile);
        UtilityUser created = reopened.create("new-user", "password");

        assertEquals(created.getAccountNumber(),
                new UtilityStore(utilityFile).find("new-user").getAccountNumber());
    }

    @Test
    public void rejectsEmptyAccountRecord() throws IOException {
        Files.writeString(utilityFile, "[{}]");

        assertThrows(IOException.class, () -> new UtilityStore(utilityFile));
    }

    @Test
    public void assignsDistinctAccountNumbers() throws IOException {
        UtilityUser second = store.create("second-user", "second-password");

        assertNotEquals(utility.getAccountNumber(), second.getAccountNumber());
    }

    @Test
    public void reloadsFirstOfMultipleAccounts() throws IOException {
        store.create("second-user", "second-password");
        UtilityStore reopened = new UtilityStore(utilityFile);

        assertEquals(utility.getAccountNumber(),
                reopened.find(utility.getUsername()).getAccountNumber());
    }

    @Test
    public void reloadsSecondOfMultipleAccounts() throws IOException {
        UtilityUser second = store.create("second-user", "second-password");
        UtilityStore reopened = new UtilityStore(utilityFile);

        assertEquals(second.getUsername(),
                reopened.find(second.getAccountNumber()).getUsername());
    }
}
