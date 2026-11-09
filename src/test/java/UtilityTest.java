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

// This class was mostly gnerated by chatgpt but heavily changed by me(Ryan)

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
    public void createsUtilityAccountWithSixDigitNumberAndFirstBill() {
        assertTrue(utility.getAccountNumber().matches("[1-9][0-9]{5}"));
        assertTrue(utility.getPaidBills().isEmpty());
        assertEquals(100, utility.getNextBill().getAmount());
        assertNotNull(utility.getNextBill().getDueDate());
    }

    @Test
    public void findsUtilityAccountByUsernameOrAccountNumber() {
        assertSame(utility, store.find("testUtility"));
        assertSame(utility, store.find(utility.getAccountNumber()));
        assertNull(store.find("unknown-user"));
    }

    @Test
    public void paymentMovesBillToHistoryAndCreatesNextBill() {
        Bill firstBill = utility.getNextBill();

        utility.payNextBill();

        assertEquals(1, utility.getPaidBills().size());
        assertSame(firstBill, utility.getPaidBills().get(0));
        assertNotNull(firstBill.getPaidAt());

        Bill nextBill = utility.getNextBill();
        assertEquals(100, nextBill.getAmount());
        assertEquals(firstBill.getDueDate().plusMonths(1), nextBill.getDueDate());
        assertNull(nextBill.getPaidAt());
    }

    @Test
    public void savesPaymentHistoryAndNextBill() throws IOException {
        utility.payNextBill();
        utility.payNextBill();
        store.save();

        UtilityStore reopened = new UtilityStore(utilityFile);
        UtilityUser saved = reopened.find(utility.getAccountNumber());

        assertNotNull(saved);
        assertEquals("testUtility", saved.getUsername());
        assertEquals(2, saved.getPaidBills().size());
        assertNotNull(saved.getPaidBills().get(0).getPaidAt());
        assertNotNull(saved.getPaidBills().get(1).getPaidAt());
        assertEquals(utility.getNextBill().getDueDate(), saved.getNextBill().getDueDate());
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
    public void reloadsMultipleAccounts() throws IOException {
        UtilityUser second = store.create("second-user", "second-password");
        UtilityStore reopened = new UtilityStore(utilityFile);

        assertNotEquals(utility.getAccountNumber(), second.getAccountNumber());
        assertEquals(utility.getAccountNumber(),
                reopened.find(utility.getUsername()).getAccountNumber());
        assertEquals(second.getUsername(),
                reopened.find(second.getAccountNumber()).getUsername());
    }
}
