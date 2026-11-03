package accounts;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class BankUser {

    private String username;
    private String password;
    @JsonManagedReference
    private ArrayList<AbstractAccount> accounts = new ArrayList<>();

    public BankUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
