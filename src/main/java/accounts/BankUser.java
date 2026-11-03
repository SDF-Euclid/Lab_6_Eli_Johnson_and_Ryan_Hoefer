package accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class BankUser {

    private String username;
    private String password;
    private int utilityNumber;
    private ArrayList<AbstractAccount> accounts = new ArrayList<>();

    public BankUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
