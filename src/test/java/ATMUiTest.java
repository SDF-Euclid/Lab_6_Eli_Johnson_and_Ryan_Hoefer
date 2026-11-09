import accounts.AccountType;
import accounts.BankUser;
import accounts.CheckingAccount;
import accounts.SavingsAccount;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

// Generated with ChatGPT. Verified output, changed one method as commented below.
public class ATMUiTest {
    private static final Pattern UI_RESULTS = Pattern.compile(
            "Account amount: \\d+|(?<=Amount to deposit: )Successfully deposited \\d+ into this account\\."
                    + "|Cannot deposit more than \\d+ in 24 hours\\."
                    + "|Cannot transfer more than \\$\\d+ from this account\\."
                    + "|Daily transfer amount for SAVINGS is \\d+");

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void functionalTr1TransfersFromCheckingToSavings() throws IOException {
        assertEquals(List.of("Account amount: 0", "Account amount: 500"),
                runAtm(500, 0, "1", "4", "500"));
    }

    /**
     * changed this method to transfer $100 instead of $500 as 100 is the limit that can be transfered daily from the savings account
     * @throws IOException
     */
    @Test
    public void functionalTr2TransfersFromSavingsToChecking() throws IOException {
        assertEquals(List.of("Account amount: 100", "Account amount: 0"),
                runAtm(0, 100, "2", "3", "100"));
    }

    @Test
    public void functionalTr3RejectsTransferWithInsufficientFunds() throws IOException {
        assertEquals(List.of("Cannot transfer more than $25 from this account.",
                        "Account amount: 25", "Account amount: 0"),
                runAtm(25, 0, "1", "4", "50"));
    }

    @Test
    public void boundaryTr1AcceptsDepositOf4999() throws IOException {
        assertEquals(List.of("Successfully deposited 4999 into this account.",
                        "Account amount: 4999", "Account amount: 0"),
                runAtm(0, 0, "1", "3", "4999"));
    }

    @Test
    public void boundaryTr2AcceptsDepositOf5000() throws IOException {
        assertEquals(List.of("Successfully deposited 5000 into this account.",
                        "Account amount: 5000", "Account amount: 0"),
                runAtm(0, 0, "1", "3", "5000"));
    }

    @Test
    public void boundaryTr3RejectsDepositOf5001() throws IOException {
        assertEquals(List.of("Cannot deposit more than 5000 in 24 hours.",
                        "Account amount: 0", "Account amount: 0"),
                runAtm(0, 0, "1", "3", "5001"));
    }

    private List<String> runAtm(long startingChecking, long startingSavings,
                                String accountOption, String actionOption, String amount) throws IOException {
        Path directory = temp.getRoot().toPath();
        UserStore userStore = new UserStore(directory.resolve("users.json"));
        UtilityStore utilityStore = new UtilityStore(directory.resolve("utilities.json"));

        BankUser user = new BankUser("test-user", "test-password");
        CheckingAccount checking = new CheckingAccount(user, AccountType.CHECKING);
        SavingsAccount savings = new SavingsAccount(user, AccountType.SAVINGS);
        user.getAccounts().add(checking);
        user.getAccounts().add(savings);
        if (startingChecking > 0) {
            checking.deposit(startingChecking);
        }
        if (startingSavings > 0) {
            savings.deposit(startingSavings);
        }
        userStore.add(user);

        String input = String.join("\n",
                "2", "test-user", "test-password", // Log in.
                accountOption, actionOption, amount,
                "1", "1",                         // View checking balance.
                "2", "1",                         // View savings balance.
                "3", "4") + "\n";                 // Log out and quit.

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOut);
            new ATM(userStore, utilityStore).run();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        return UI_RESULTS.matcher(output.toString(StandardCharsets.UTF_8))
                .results()
                .map(MatchResult::group)
                .toList();
    }
}
