package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SavingsAccount extends AbstractAccount {

    private static final int DAILY_TRANSFER_AMOUNT = 100;

    public SavingsAccount(BankUser accountOwner, AccountType accountType) {super(accountOwner, accountType);}

    public void transfer(long amount, AccountType transferAccountType) {
        if (transferAccountType == this.accountType) throw new IllegalArgumentException("Transfer must be from one unique account to another.");
        if (amount < 0) throw new IllegalArgumentException("Transfer amount cannot be negative.");
        if (balance < amount) throw new IllegalArgumentException("Cannot transfer more than $" + balance + " from this account.");
        if (amount + totalInLast24Hours(TransactionType.TRANSFER, true) > DAILY_TRANSFER_AMOUNT) throw new IllegalArgumentException("Daily transfer amount for " + accountType + " is " + DAILY_TRANSFER_AMOUNT);

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
        System.out.print("successfully transferred $" + amount + " from this account into your checking account.");
    }

}
