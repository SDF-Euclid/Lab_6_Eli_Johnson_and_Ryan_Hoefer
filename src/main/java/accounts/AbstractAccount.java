package accounts;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CheckingAccount.class, name = "checking"),
        @JsonSubTypes.Type(value = SavingsAccount.class, name = "savings")
})
public abstract class AbstractAccount {

    @JsonBackReference
    protected User accountOwner;
    protected static final long DAILY_DEPOSIT_AMOUNT = 5000;
    protected ArrayList<Transaction> transactions = new ArrayList<>();
    protected AccountType accountType;
    protected long balance;

    public AbstractAccount(User accountOwner, AccountType accountType) {
        for (AbstractAccount account: accountOwner.getAccounts()) {
            if (account.getAccountType() == accountType) throw new IllegalArgumentException("The owner already has an account of type " + account.getAccountType());
        }
        this.accountOwner = accountOwner;
        this.accountType = accountType;
        this.balance = 0;
    }

    public void deposit(long amount) {
        if (amount + totalInLast24Hours(TransactionType.DEPOSIT, false) > DAILY_DEPOSIT_AMOUNT) throw new IllegalArgumentException("Cannot deposit more than " + DAILY_DEPOSIT_AMOUNT + " in 24 hours.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        balance += amount;
        transactions.add(new Transaction(accountType, TransactionType.DEPOSIT, amount));
        System.out.print("Successfully deposited " + amount + " into this account.");
    }

    protected long totalInLast24Hours(TransactionType type, boolean outgoing) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        long total = 0;

        for (Transaction transaction : transactions) {
            long amount = transaction.getTransactionAmount();

            if (transaction.getTransactionType() == type
                    && transaction.getTimestamp().isAfter(cutoff)
                    && (amount < 0) == outgoing) {
                total += Math.abs(amount);
            }
        }

        return total;
    }

    @Override
    public String toString() {
        return accountType.toString();
    }
}
