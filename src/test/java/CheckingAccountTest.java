import accounts.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * This test class was human written by me(Ryan)
 */
public class CheckingAccountTest {
    CheckingAccount checking;
    SavingsAccount saving;
    UtilityUser utility;

    @Before
    public void setUp() throws Exception {
        BankUser user = new BankUser("test-user", "test-password");
        checking = new CheckingAccount(user, AccountType.CHECKING);
        saving = new SavingsAccount(user, AccountType.SAVINGS);
        utility = new UtilityUser("testUtility", "password", "123456");

        user.getAccounts().add(checking);
        user.getAccounts().add(saving);

    }

    @Test
    public void testWithdrawl() {
        checking.deposit(1000);

        checking.withdraw(499);
        checking.withdraw(1);
        assertEquals(500, checking.getBalance());

    }

    @Test
    public void rejectWithdrawl() {
        checking.deposit(1000);

        assertThrows(IllegalArgumentException.class, () -> checking.withdraw(1001));



    }
    @Test
    public void rejectMoreThan5000Deposit() {
        assertThrows(IllegalArgumentException.class, () ->         checking.deposit(5001));

    }

    @Test
    public void rejectMoreThan5000DepositConsecutiveSums() {
        checking.deposit(1000);
        assertThrows(IllegalArgumentException.class, () ->         checking.deposit(4001));

    }

    @Test
    public void rejectMoreThan500WithdrawConsecutiveSums() {
        checking.deposit(1000);
        checking.withdraw(100);
        assertThrows(IllegalArgumentException.class, () ->         checking.withdraw(401));

    }

    @Test
    public void transferToCheckingAccount() {
        checking.deposit(1000);
        checking.transfer(1000, AccountType.SAVINGS);
        assertEquals(1000, saving.getBalance());
    }

    @Test
    public void rejectTransferToCheckingAccount() {
        checking.deposit(1000);
        assertThrows(IllegalArgumentException.class, () ->         checking.transfer(1001, AccountType.SAVINGS));

    }

    @Test
    public void payUtilityBill() {
        checking.deposit(1000);
        checking.payUtilities(utility);
        assertEquals(900, checking.getBalance());

    }

    @Test
    public void rejectUtilityBill() {
        checking.deposit(90);
        assertThrows(IllegalArgumentException.class, () ->      checking.payUtilities(utility)   );

    }


}
