package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class Transaction {

    private AccountType accountType;
    private TransactionType transactionType;
    private long transactionAmount;
    private Instant timestamp;

    public Transaction(AccountType accountType, TransactionType transactionType, long transactionAmount) {
        this.accountType = accountType;
        this.transactionType = transactionType;
        this.transactionAmount = transactionAmount;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "AccountType: " + accountType
                + ", TransactionType: " + transactionType
                + ", Amount: " + transactionAmount
                + ", Timestamp: " + timestamp;
    }
}
