package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private ArrayList<AbstractAccount> accounts = new ArrayList<AbstractAccount>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void createAccount(AccountType accountType) {
        if (accountType.equals(AccountType.CHECKING)) {
            accounts.add(new CheckingAccount(this, accountType));
        }
        else {
            accounts.add(new SavingsAccount(this, accountType));
        }
    }
}
