package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class Bill {
    private long amount;
    private LocalDate dueDate;
    private Instant paidAt;

    public Bill(long amount, LocalDate dueDate) {
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public void markPaid() {
        paidAt = Instant.now();
    }
}