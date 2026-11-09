import accounts.AccountType;
import accounts.BankUser;
import accounts.CheckingAccount;
import accounts.SavingsAccount;
import accounts.TransactionType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Generated with ChatGPT, had it re-write to only have one assertion per test. Then verified each test.
public class UserStoreTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    BankUser userFromFile;

    @Before
    public void setUp() throws IOException {

        Path usersFile = temp.getRoot().toPath().resolve("users.json");

        UserStore store = new UserStore(usersFile);
        BankUser user = new BankUser("test-user", "test-password");

        CheckingAccount checking = new CheckingAccount(user, AccountType.CHECKING);
        SavingsAccount savings = new SavingsAccount(user, AccountType.SAVINGS);

        user.getAccounts().add(checking);
        user.getAccounts().add(savings);


        checking.deposit(1000);
        savings.deposit(250);
        store.add(user);

        this.userFromFile = new UserStore(usersFile).findByUsername("test-user");
    }

    @Test
    public void reloadsUserByUsername() {
        assertNotNull(userFromFile);
    }

    @Test
    public void reloadsBothAccounts() {
        assertEquals(2, userFromFile.getAccounts().size());
    }

    @Test
    public void reloadsCheckingAccountType() {
        assertEquals(AccountType.CHECKING, userFromFile.getAccounts().get(0).getAccountType());
    }

    @Test
    public void reloadsCheckingBalance() {
        assertEquals(1000, userFromFile.getAccounts().get(0).getBalance());
    }

    @Test
    public void reloadsCheckingTransactionCount() {
        assertEquals(1, userFromFile.getAccounts().get(0).getTransactions().size());
    }

    @Test
    public void reloadsCheckingDepositType() {
        assertEquals(TransactionType.DEPOSIT, userFromFile.getAccounts().get(0).getTransactions().get(0).getTransactionType());
    }

    @Test
    public void reloadsCheckingDepositAmount() {
        assertEquals(1000, userFromFile.getAccounts().get(0).getTransactions().get(0).getTransactionAmount());
    }

    @Test
    public void reloadsCheckingDepositTimestamp() {
        assertNotNull(userFromFile.getAccounts().get(0).getTransactions().get(0).getTimestamp());
    }

    @Test
    public void reloadsSavingsAccountType() {
        assertEquals(AccountType.SAVINGS, userFromFile.getAccounts().get(1).getAccountType());
    }

    @Test
    public void reloadsSavingsBalance() {
        assertEquals(250, userFromFile.getAccounts().get(1).getBalance());
    }

    @Test
    public void reloadsSavingsTransactionCount() {
        assertEquals(1, userFromFile.getAccounts().get(1).getTransactions().size());
    }

    @Test
    public void reloadsSavingsDepositType() {
        assertEquals(TransactionType.DEPOSIT, userFromFile.getAccounts().get(1).getTransactions().get(0).getTransactionType());
    }

    @Test
    public void reloadsSavingsDepositAmount() {
        assertEquals(250, userFromFile.getAccounts().get(1).getTransactions().get(0).getTransactionAmount());
    }

    @Test
    public void reloadsSavingsDepositTimestamp() {
        assertNotNull(userFromFile.getAccounts().get(1).getTransactions().get(0).getTimestamp());
    }
}
