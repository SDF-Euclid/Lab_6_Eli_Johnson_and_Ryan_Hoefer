import accounts.User;
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
    private List<User> users = new ArrayList<>();

    public UserStore(Path usersFile) throws IOException {
        this.usersFile = usersFile;
        if (Files.exists(usersFile)) {
            users = mapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {});
        }
    }

    public User findByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void add(User user) throws IOException {
        users.add(user);
        save();
    }

    public void save() throws IOException {
        Files.createDirectories(usersFile.getParent());
        mapper.writerWithDefaultPrettyPrinter().writeValue(usersFile.toFile(), users);
    }
}