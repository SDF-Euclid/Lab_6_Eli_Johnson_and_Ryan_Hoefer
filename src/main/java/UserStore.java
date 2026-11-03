import accounts.BankUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UserStore {
    private final Path usersFile;
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private List<BankUser> bankUsers = new ArrayList<>();

    public UserStore(Path usersFile) throws IOException {
        this.usersFile = usersFile;
        if (Files.exists(usersFile)) {
            bankUsers = mapper.readValue(usersFile.toFile(), new TypeReference<List<BankUser>>() {});
        }
    }

    public BankUser findByUsername(String username) {
        for (BankUser bankUser : bankUsers) {
            if (bankUser.getUsername().equals(username)) {
                return bankUser;
            }
        }
        return null;
    }

    public void add(BankUser bankUser) throws IOException {
        bankUsers.add(bankUser);
        save();
    }

    public void save() throws IOException {
        Files.createDirectories(usersFile.getParent());
        mapper.writerWithDefaultPrettyPrinter().writeValue(usersFile.toFile(), bankUsers);
    }
}