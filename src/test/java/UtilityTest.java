import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class UtilityTest {
    @Before
    public void before() throws IOException {
        UserStore userStore = new UserStore(Path.of("data", "users.json"));
        UtilityStore utilityStore = new UtilityStore(Path.of("data", "utilities.json"));
        new ATM(userStore, utilityStore).run();
    }

    @Test
    public void testUtility() throws IOException {
        

    }
}
