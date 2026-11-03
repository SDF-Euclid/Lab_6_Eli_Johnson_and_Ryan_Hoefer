package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class UtilityUser {
    private String username;
    private String password;
    private String accountNumber;
    private List<Bill> paidBills = new ArrayList<>();
    private Bill nextBill;

    public UtilityUser(String username, String password, String accountNumber) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
        this.nextBill = new Bill(100, LocalDate.now().plusMonths(1));
    }

    public void payNextBill() {
        nextBill.markPaid();
        paidBills.add(nextBill);
        nextBill = new Bill(nextBill.getAmount(), nextBill.getDueDate().plusMonths(1));
    }
}