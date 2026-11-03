import accounts.UtilityUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UtilityStore {
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private List<UtilityUser> accounts = new ArrayList<>();

    public UtilityStore(Path file) throws IOException {
        this.file = file;
        if (Files.exists(file)) {
            accounts = mapper.readValue(file.toFile(), new TypeReference<List<UtilityUser>>() {});
        }
    }

    public static UtilityUser find(String usernameOrNumber) {
        for (UtilityUser account : accounts) {
            if (account.getUsername().equals(usernameOrNumber)
                    || account.getAccountNumber().equals(usernameOrNumber)) {
                return account;
            }
        }
        return null;
    }

    public static UtilityUser create(String username, String password) throws IOException {
        String accountNumber;
        do {
            accountNumber = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        } while (find(accountNumber) != null);

        UtilityUser account = new UtilityUser(username, password, accountNumber);
        accounts.add(account);
        save();
        return account;
    }

    public static void save() throws IOException {
        Files.createDirectories(file.getParent());
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), accounts);
    }
}