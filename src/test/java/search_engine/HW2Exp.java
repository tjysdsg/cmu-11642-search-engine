package search_engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HW2Exp {
    private static final String EXP_FILE_DIR = "experiments/hw2";
    private static final String OUTPUT_DIR = "exp/";
    private static final String[] SECTION1_EXP_IDS = {
            "1.1a",
            "1.1b",
            "1.1c",
    };

    private static final String[] SECTION2_EXP_IDS = {
            "2.1a",
            "2.1b",
            "2.1c",
            "2.1d",
            "2.1e",
            "2.2a",
            "2.2b",
            "2.2c",
            "2.2d",
            "2.2e",
    };

    private static final String[] SECTION3_EXP_IDS = {
            "3.1a",
            "3.1b",
            "3.1c",
            "3.1d",
            "3.1e",
    };

    private static final String[] SECTION4_EXP_IDS = {
            "4.1a",
            "4.1b",
            "4.1c",
            "4.1d",
            "4.1e",
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
            runSearchEngine("HW2-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection2() throws Exception {
        for (String expID : SECTION2_EXP_IDS) {
            runSearchEngine("HW2-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection3() throws Exception {
        for (String expID : SECTION3_EXP_IDS) {
            runSearchEngine("HW2-Exp-" + expID + ".param");
        }
    }

    @Test
    void testSection4() throws Exception {
        for (String expID : SECTION4_EXP_IDS) {
            runSearchEngine("HW2-Exp-" + expID + ".param");
        }
    }
}
