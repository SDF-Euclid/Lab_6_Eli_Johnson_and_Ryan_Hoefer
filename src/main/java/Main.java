import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        UserStore userStore = new UserStore(Path.of("data", "users.json"));
        UtilityStore utilityStore = new UtilityStore(Path.of("data", "utilities.json"));
        new ATM(userStore, utilityStore).run();
    }
}
