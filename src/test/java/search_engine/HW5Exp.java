package search_engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HW5Exp {
    private static final String EXP_FILE_DIR = "experiments/hw5";
    private static final String OUTPUT_DIR = "exp/";
    private static final String[] EXP_IDS = {
            "1.1a",
            "1.1b",
            "1.1c",
            "1.1d",
            "1.1e",
            "1.1f",

            "2.1a",
            "2.1b",
            "2.1c",
            "2.1d",

            "2.2a",
            "2.2b",
            "2.2c",
            "2.2d",

            "2.3a",
            "2.3b",
            "2.3c",
            "2.3d",

            "2.4a",
            "2.4b",
            "2.4c",
            "2.4d",

            "3.1a",
            "3.1b",
            "3.1c",
            "3.1d",

            "3.2a",
            "3.2b",
            "3.2c",
            "3.2d",

            "3.3a",
            "3.3b",
            "3.3c",
            "3.3d",
    };

    void runSearchEngine(String paramFile) throws Exception {
        Path param_path = Paths.get(EXP_FILE_DIR, paramFile);
        Path tmp_param_path = Paths.get(OUTPUT_DIR, paramFile);
        String content = new String(Files.readAllBytes(param_path));
        content = content.replaceAll("INPUT_DIR", "E:/");
        content = content.replaceAll("TEST_DIR", EXP_FILE_DIR);
        content = content.replaceAll("OUTPUT_DIR", OUTPUT_DIR);
        Files.write(tmp_param_path, content.getBytes());

        QryEval.main(new String[]{tmp_param_path.toString()});
    }

    @BeforeAll
    static void setUp() throws IOException {
        Path dir = Paths.get(OUTPUT_DIR);
        if (!Files.exists(dir))
            Files.createDirectories(dir);
    }

    @Test
    void test() throws Exception {
        for (String expID : EXP_IDS) {
            System.out.println("\nEXP_ID: " + expID);
            runSearchEngine("HW5-Exp-" + expID + ".param");
        }
    }
}
