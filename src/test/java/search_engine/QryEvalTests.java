package search_engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;


public abstract class QryEvalTests {
    protected static final String TEST_FILE_DIR = "src/test/resources/";
    protected static final String OUTPUT_DIR = "tmp/";

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void test(final String fileName) throws Exception {
        Path dir = Paths.get(OUTPUT_DIR);
        if (!Files.exists(dir))
            Files.createDirectories(dir);

        String outFile = OUTPUT_DIR + fileName + ".teIn";
        Files.deleteIfExists(Paths.get(outFile));

        // normalize paths in *.param file
        Path param_path = Paths.get(TEST_FILE_DIR, fileName + ".param");
        Path tmp_param_path = Paths.get(OUTPUT_DIR, fileName + ".param");
        String content = new String(Files.readAllBytes(param_path));
        content = content.replaceAll("INPUT_DIR", "E:/");
        content = content.replaceAll("TEST_DIR", TEST_FILE_DIR);
        content = content.replaceAll("OUTPUT_DIR", OUTPUT_DIR);
        Files.write(tmp_param_path, content.getBytes());

        // RUN
        QryEval.main(new String[]{tmp_param_path.toString()});

        checkResult(fileName);
    }

    protected void checkResult(String testName) throws Exception {
        checkRankingResult(testName);
        // add other tests here
    }

    private void checkRankingResult(String testName) throws IOException {
        // read answer file
        String ansFile = TEST_FILE_DIR + testName + ".teIn";
        List<RankEntry> ans = Utils.readRankingFile(ansFile);

        // check search engine output
        String outFile = OUTPUT_DIR + testName + ".teIn";
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(outFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                String[] substrings = line.split(" ");
                assertEquals(6, substrings.length);

                String qryID = substrings[0];
                String docID = substrings[2];
                int rank = Integer.parseInt(substrings[3]);
                double score = Double.parseDouble(substrings[4]);

                assertEquals(ans.get(i).qryID, qryID);
                assertEquals(ans.get(i).docID, docID);
                assertEquals(ans.get(i).rank, rank);
                assertEquals(ans.get(i).score, score, 1E-6);

                ++i;
            }
        }
        assertEquals(i, ans.size());

        System.out.println("=> Retrieval ranking tests passed");
    }

    private static Stream<String> fileNameSource() {
        // return IntStream.range(0, 0).mapToObj(i -> "" + i);
        assert false;
        return null;
    }
}
