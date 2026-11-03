package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckingAccount extends AbstractAccount{

    private static final long DAILY_WITHDRAW_AMOUNT = 500;

    public CheckingAccount(BankUser accountOwner, AccountType accountType) {super(accountOwner, accountType);}

    public void withdraw(long amount) {
        if (balance < amount) throw new IllegalArgumentException("Cannot withdraw more than $" + balance + " from this account.");
        if (amount + totalInLast24Hours(TransactionType.WITHDRAW, true) > DAILY_WITHDRAW_AMOUNT) throw new IllegalArgumentException("Daily withdraw amount cannot exceed $" + DAILY_WITHDRAW_AMOUNT + ".");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        balance -= amount;
        transactions.add(new Transaction(accountType, TransactionType.WITHDRAW, -1 * amount));
        System.out.print("successfully withdrew $" + amount + " from this account.");
    }

    public void transfer(long amount, AccountType transferAccountType) {
        if (transferAccountType == this.accountType) throw new IllegalArgumentException("Transfer must be from one unique account to another.");
        if (amount < 0) throw new IllegalArgumentException("Transfer amount cannot be negative.");
        if (balance < amount) throw new IllegalArgumentException("Cannot transfer more than $" + balance + " from this account.");

        AbstractAccount transferAccount = null;
        for (AbstractAccount account : accountOwner.getAccounts()) {
            if (account.getAccountType() == transferAccountType) {
                transferAccount = account;
                break;
            }
        }

        if (transferAccount == null) throw new IllegalArgumentException("BankUser does not have an account of type " + transferAccountType);

        transferAccount.deposit(amount);
        transferAccount.transactions.add(new Transaction(accountType, TransactionType.TRANSFER, amount));
        this.balance -= amount;
        transactions.add(new Transaction(accountType, TransactionType.TRANSFER, -1 * amount));
        System.out.print("successfully transferred $" + amount + " from this account into your savings account.");
    }

    public void payUtilities() {

    }
}
