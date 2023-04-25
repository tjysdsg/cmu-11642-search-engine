package search_engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HW3Exp {
    private static final String EXP_FILE_DIR = "experiments/hw3";
    private static final String OUTPUT_DIR = "exp/";
    private static final String[] SECTION1_EXP_IDS = {
            "1a",
            "1b",
            "1c",
    };

    private static final String[] SECTION2_EXP_IDS = {
            "2a",
            "2b",
            "2c",
    };

    private static final String[] SECTION3_EXP_IDS = {
            "3a",
            "3b",
            "3c",
            "3d",
    };

    private static final String[] SECTION4_EXP_IDS = {
            "4a",
            "4b",
            "4c",
            "4d",
            "4e",
            "4f",
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
    void testSection1() throws Exception {
        for (String expID : SECTION1_EXP_IDS) {
            System.out.println("\nEXP_ID: " + expID);
            runSearchEngine("HW3-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection2() throws Exception {
        for (String expID : SECTION2_EXP_IDS) {
            System.out.println("\nEXP_ID: " + expID);
            runSearchEngine("HW3-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection3() throws Exception {
        for (String expID : SECTION3_EXP_IDS) {
            System.out.println("\nEXP_ID: " + expID);
            runSearchEngine("HW3-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection4() throws Exception {
        for (String expID : SECTION4_EXP_IDS) {
            System.out.println("\nEXP_ID: " + expID);
            runSearchEngine("HW3-Exp-" + expID + ".param");
        }
    }
}
