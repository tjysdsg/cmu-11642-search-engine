package search_engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public abstract class QryEvalLTRTests extends QryEvalTests {
    @Override
    protected void checkResult(String testName) throws Exception {
        checkLTRResult(testName, "Train");
        checkLTRResult(testName, "Test");

        super.checkResult(testName);
    }

    private void checkLTRResult(String testName, String ltrSuffix) throws Exception {
        String ansFile = TEST_FILE_DIR + testName + ".Ltr" + ltrSuffix;
        var ans = readLTRFeatureFile(ansFile);

        String outFile = OUTPUT_DIR + testName + ".Ltr" + ltrSuffix;
        var output = readLTRFeatureFile(outFile);

        for (var pair : ans.entrySet()) {
            int qid = pair.getKey();
            LTRFeatureVectorList ansFeatList = pair.getValue();

            LTRFeatureVectorList outFeatList = output.getOrDefault(qid, null);
            assertNotNull(outFeatList);

            checkLTRFeatList(ansFeatList, outFeatList);
        }
        System.out.println("=> LTR feature vector tests passed");
    }

    private void checkLTRFeatList(LTRFeatureVectorList ansFeatList, LTRFeatureVectorList outFeatList) {
        assertEquals(ansFeatList.size(), outFeatList.size());
        for (int i = 0; i < ansFeatList.size(); ++i) {
            var ans = ansFeatList.get(i);
            var out = outFeatList.get(i);

            assertEquals(ans.externalId, out.externalId);
            assertEquals(ans.docid, out.docid);
            assertEquals(ans.qryID, out.qryID);
            assertEquals(ans.relevance, out.relevance);

            // feature vectors
            assertTrue(out.featVector.size() >= ans.featVector.size());
            for (var e : ans.featVector.entrySet()) {
                int idx = e.getKey();
                double val = e.getValue();
                assertEquals(val, out.featVector.get(idx), 1e-6, "Incorrect feature at index: " + idx);
            }
        }
    }

    private Map<Integer, LTRFeatureVectorList> readLTRFeatureFile(String path) throws Exception {
        Map<Integer, LTRFeatureVectorList> ret = new HashMap<>();

        LTRFeatureVectorList featList = new LTRFeatureVectorList();
        int prevQid = -1;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                String[] substrings = line.split("\\s+");
                assertTrue(line.length() > 4); // relevance qid [feats...] # externalID

                int relevance = Integer.parseInt(substrings[0]);

                String[] qidStrings = substrings[1].split(":");
                assertEquals(2, qidStrings.length);
                assertEquals("qid", qidStrings[0]);
                int qid = Integer.parseInt(qidStrings[1]);

                if (qid != prevQid && prevQid != -1) {
                    ret.put(prevQid, featList);
                    featList = new LTRFeatureVectorList();
                }
                prevQid = qid;

                Map<Integer, Double> vec = new HashMap<>();
                for (int i = substrings.length - 3; i >= 2; --i) { // loop backwards so we can initialize vec with the largest size
                    String[] featureStrings = substrings[i].split(":");
                    assertEquals(2, featureStrings.length);

                    int featIdx = Integer.parseInt(featureStrings[0]); // starting from 1
                    double val = Double.parseDouble(featureStrings[1]);
                    vec.put(featIdx - 1, val);
                }
                String externalID = substrings[substrings.length - 1];
                int docid = Idx.getInternalDocid(externalID);
                featList.add(qid, docid, externalID, vec, relevance);
            }
        }

        ret.put(prevQid, featList);
        return ret;
    }
}
