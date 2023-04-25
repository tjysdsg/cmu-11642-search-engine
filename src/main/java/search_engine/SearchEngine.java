package search_engine;

import java.io.*;
import java.util.*;

public class SearchEngine {
    private RetrievalModel model = null;
    private String retrievalAlgorithm = null;
    private Map<String, String> parameters = null;
    private String prf = null;
    private String diversityAlgorithm = null;
    private String expansionField = null;
    private String expansionQryOutFile = null;
    private String initialRankingFile = null;
    private String diversityInitialRankingFile = null;
    private int evalOutputLength = 1000;

    public RetrievalModel getModel() {
        return model;
    }

    /**
     * Allocate the retrieval model and initialize it using parameters.
     */
    public SearchEngine(Map<String, String> parameters) throws IOException {
        this.parameters = parameters;
        Idx.open(parameters.get("indexPath"));

        retrievalAlgorithm = parameters.get("retrievalAlgorithm").toLowerCase();
        if (retrievalAlgorithm.equals("unrankedboolean")) {
            model = new RetrievalModelUnrankedBoolean();
        } else if (retrievalAlgorithm.equals("rankedboolean")) {
            model = new RetrievalModelRankedBoolean();
        } else if (retrievalAlgorithm.equals("bm25")) {
            double b = Double.parseDouble(parameters.get("BM25:b"));
            double k1 = Double.parseDouble(parameters.get("BM25:k_1"));
            double k3 = Double.parseDouble(parameters.get("BM25:k_3"));
            model = new BM25(b, k1, k3);
        } else if (retrievalAlgorithm.equals("indri")) {
            double mu = Double.parseDouble(parameters.get("Indri:mu"));
            double lambda = Double.parseDouble(parameters.get("Indri:lambda"));
            model = new Indri(mu, lambda);
        } else if (retrievalAlgorithm.equals("ltr")) {
            double mu = Double.parseDouble(parameters.get("Indri:mu"));
            double lambda = Double.parseDouble(parameters.get("Indri:lambda"));
            Indri indri = new Indri(mu, lambda);

            double b = Double.parseDouble(parameters.get("BM25:b"));
            double k1 = Double.parseDouble(parameters.get("BM25:k_1"));
            double k3 = Double.parseDouble(parameters.get("BM25:k_3"));
            BM25 bm25 = new BM25(b, k1, k3);

            model = new LTR(indri, bm25);
        } else {
            throw new IllegalArgumentException("Unknown retrieval model " + retrievalAlgorithm);
        }

        // Read params
        prf = parameters.getOrDefault("prf", "false"); // Pseudo Relevance Feedback (PRF)
        expansionField = parameters.getOrDefault("prf:expansionField", "body");
        expansionQryOutFile = parameters.getOrDefault("prf:expansionQueryFile", "");
        initialRankingFile = parameters.getOrDefault("prf:initialRankingFile", "");
        evalOutputLength = Integer.parseInt(parameters.get("trecEvalOutputLength"));

        diversityInitialRankingFile = parameters.getOrDefault("diversity:initialRankingFile", "");
        String diversity = parameters.getOrDefault("diversity", "false").toLowerCase();
        if (diversity.equals("true")) {
            diversityAlgorithm = parameters.get("diversity:algorithm").toLowerCase();
        }
    }

    public Map<String, ScoreList> run() throws Exception {
        if (model instanceof LTR) {
            return runLTR();
        } else if (diversityAlgorithm != null) { // diversity
            return runDiversity();
        } else if (prf.equals("false")) { // Vanilla
            return processQueryFile(parameters.get("queryFilePath"));
        } else if (prf.equals("Indri")) {
            return runIndriPRF();
        } else {
            throw new RuntimeException("Invalid prf parameter value: " + prf);
        }
    }

    public Map<String, ScoreList> runLTR() throws Exception {
        // read relevance judgement
        var relevanceJudgement = readRelevanceJudgement(parameters.get("ltr:trainingQrelsFile"));
        Map<Integer, List<String>> trainExternalIDs = new HashMap<>();
        Map<Integer, Map<String, Integer>> trainRelevance = new HashMap<>();
        for (var entry : relevanceJudgement.entrySet()) {
            int qid = entry.getKey();
            trainExternalIDs.put(qid, new ArrayList<>());
            trainRelevance.put(qid, new HashMap<>());

            for (RelevanceLabel r : entry.getValue()) {
                trainExternalIDs.get(qid).add(r.externalID);
                trainRelevance.get(qid).put(r.externalID, r.relevance);
            }
        }

        // get feature vector
        String ranklib = parameters.get("ltr:toolkit");
        boolean isSVMRank = ranklib.equals("SVMRank");
        var trainFeats = obtainLTRFeatures(
                parameters.get("ltr:trainingQueryFile"),
                trainExternalIDs,
                trainRelevance,
                parameters.getOrDefault("ltr:featureDisable", ""),
                isSVMRank,
                true
        );
        String writeFeaturePath = parameters.get("ltr:trainingFeatureVectorsFile");
        writeLTRFeatures(trainFeats, writeFeaturePath);

        // train reranking model
        System.out.println("Training the reranking model");
        String modelPath = parameters.get("ltr:modelFile");
        if (ranklib.equals("RankLib")) {
            String rankLibModel = parameters.get("ltr:RankLib:model");
            List<String> args = new ArrayList<>(List.of(new String[]{
                    "-ranker", rankLibModel,
                    "-train", writeFeaturePath,
                    "-save", modelPath,
            }));
            if (rankLibModel.equals("4")) {
                String metric = parameters.get("ltr:RankLib:metric2t");
                assert metric != null;
                args.add("-metric2t");
                args.add(metric);
            }
            ciir.umass.edu.eval.Evaluator.main(args.toArray(new String[0]));
        } else if (isSVMRank) {
            Utils.runExternalProcess(
                    "svm_rank_learn",
                    new String[]{
                            parameters.get("ltr:svmRankLearnPath"),
                            "-c", parameters.get("ltr:svmRankParamC"),
                            writeFeaturePath,
                            modelPath}
            );
        } else {
            assert false;
        }

        // initial ranking of the test query
        String initialRankingFile = parameters.get("ltr:initialRankingFile");
        Map<String, ScoreList> initialRanking;
        if (initialRankingFile == null) {
            initialRanking = processQueryFile(parameters.get("queryFilePath"));
        } else {
            initialRanking = Utils.readRankingFileAsScoreList(initialRankingFile);
        }

        // get features of the test query
        Map<Integer, List<String>> testExternalIDs = new LinkedHashMap<>(); // PRESERVE ORDER
        for (var entry : initialRanking.entrySet()) {
            int qid = Integer.parseInt(entry.getKey());
            testExternalIDs.put(qid, new ArrayList<>());

            ScoreList scoreList = entry.getValue();
            for (int i = 0; i < scoreList.size(); ++i) {
                testExternalIDs.get(qid).add(scoreList.getExternalDocid(i));
            }
        }
        var testFeats = obtainLTRFeatures(
                parameters.get("queryFilePath"),
                testExternalIDs,
                new HashMap<>(),
                parameters.getOrDefault("ltr:featureDisable", ""),
                isSVMRank,
                false
        );
        String testFeaturePath = parameters.get("ltr:testingFeatureVectorsFile");
        writeLTRFeatures(testFeats, testFeaturePath);

        // Rerank
        System.out.println("Reranking using the trained model");
        String testRerankedScorePath = parameters.get("ltr:testingDocumentScores");
        if (ranklib.equals("RankLib")) {
            ciir.umass.edu.eval.Evaluator.main(
                    new String[]{
                            "-rank", testFeaturePath,
                            "-load", modelPath,
                            "-score", testRerankedScorePath,
                    }
            );
        } else if (isSVMRank) {
            Utils.runExternalProcess(
                    "svm_rank_classify",
                    new String[]{
                            parameters.get("ltr:svmRankClassifyPath"),
                            testFeaturePath,
                            modelPath,
                            testRerankedScorePath,
                    }
            );
        } else {
            assert false;
        }

        // Read the reranked scores and build the final retrieval result
        List<Double> rerankedScores = new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(testRerankedScorePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                assert tokens.length >= 1;
                rerankedScores.add(Double.parseDouble(tokens[tokens.length - 1]));
            }
        }

        int i = 0;
        LinkedHashMap<String, ScoreList> finalRanking = new LinkedHashMap<>();
        for (var entry : testFeats.entrySet()) {
            int qid = entry.getKey();
            ScoreList scoreList = new ScoreList();

            LTRFeatureVectorList feats = entry.getValue();
            for (int j = 0; j < feats.size(); ++j) {
                double newScore = rerankedScores.get(i++);
                scoreList.add(feats.getExternalDocid(j), newScore);
            }

            scoreList.sort();
            finalRanking.put(String.valueOf(qid), scoreList);
        }

        return finalRanking;
    }

    public Map<String, ScoreList> runIndriPRF() throws Exception {
        // Load initial ranking
        Map<String, ScoreList> ranking;
        if (initialRankingFile.isEmpty()) {
            ranking = processQueryFile(parameters.get("queryFilePath"));
        } else {
            ranking = Utils.readRankingFileAsScoreList(initialRankingFile);
        }

        // Initialize PRF
        var prfScores = initializePrf(
                ranking,
                expansionField,
                Integer.parseInt(parameters.get("prf:numDocs")),
                Double.parseDouble(parameters.get("prf:Indri:mu"))
        );

        // Retrieve using PRF
        return processQueryFile(
                parameters.get("queryFilePath"),
                prfScores,
                expansionField,
                Integer.parseInt(parameters.get("prf:numTerms")),
                Double.parseDouble(parameters.get("prf:Indri:origWeight")),
                expansionQryOutFile
        );
    }

    public Map<String, ScoreList> runDiversity() throws Exception {
        int inputMaxLength = Integer.parseInt(parameters.get("diversity:maxInputRankingsLength"));
        evalOutputLength = Math.max(evalOutputLength, inputMaxLength);

        // Load initial ranking
        Map<String, ScoreList> initialRanking;
        if (diversityInitialRankingFile.isEmpty()) {
            initialRanking = processQueryFile(parameters.get("queryFilePath"));
            var intentRanking = processQueryFile(parameters.get("diversity:intentsFile"));
            initialRanking.putAll(intentRanking);
        } else {
            initialRanking = Utils.readRankingFileAsScoreList(diversityInitialRankingFile);
        }

        double lambda = Double.parseDouble(parameters.get("diversity:lambda"));
        assert lambda <= 1.0 && lambda >= 0.0;

        int outputMaxLength = Integer.parseInt(parameters.get("diversity:maxResultRankingLength"));

        // Find corresponding intents of each query
        Map<String, List<String>> queryToIntents = new HashMap<>();
        List<String> queries = new ArrayList<>();
        for (var entry : initialRanking.entrySet()) {
            String k = entry.getKey();
            String[] substr = k.split("\\.");
            if (substr.length > 1) {
                queryToIntents.computeIfAbsent(substr[0], e -> new ArrayList<>()).add(k);
            } else {
                queries.add(k);
            }

            entry.getValue().truncate(inputMaxLength);
        }

        // Set intent scores of truncated documents to 0, and find the max sum of each query or intent
        Map<String, Double> qidNormalizationScales = new HashMap<>(); // qid (NOT INCLUDING intent) -> sum of scores
        for (var entry : initialRanking.entrySet()) {
            String k = entry.getKey();
            ScoreList scoreList = entry.getValue();

            String[] substr = k.split("\\.");
            String qid = k;
            if (substr.length > 1) { // intent
                qid = substr[0];
                ScoreList queryScores = initialRanking.get(qid);

                for (int i = 0; i < scoreList.size(); ++i) {
                    if (!queryScores.getDocidToIndexMapping().containsKey(
                            scoreList.getDocid(i)
                    ))
                        scoreList.setDocidScore(i, 0.0);
                }
            }

            qidNormalizationScales.putIfAbsent(qid, -Double.MAX_VALUE);
            qidNormalizationScales.put(qid, Math.max(
                    qidNormalizationScales.get(qid), scoreList.sumScores()
            ));
        }

        if (diversityAlgorithm.equals("xquad")) {
            return xQuAD(initialRanking, queries, queryToIntents, qidNormalizationScales, outputMaxLength, lambda);
        } else if (diversityAlgorithm.equals("pm2")) {
            return PM2(initialRanking, queries, queryToIntents, qidNormalizationScales, outputMaxLength, lambda);
        } else {
            throw new RuntimeException("Unknown diversity algorithm: " + diversityAlgorithm);
        }
    }

    public Map<String, ScoreList> xQuAD(
            Map<String, ScoreList> initialRanking,
            List<String> queries,
            Map<String, List<String>> queryToIntents,
            Map<String, Double> qidNormalizationScales,
            int outputMaxLength,
            double lambda
    ) throws Exception {
        Map<String, ScoreList> res = new LinkedHashMap<>(); // preserve order
        for (String qid : queries) {
            ScoreList scoreList = initialRanking.get(qid);

            // initial set of documents
            Set<Integer> initialDocs = new HashSet<>();
            for (int i = 0; i < scoreList.size(); ++i) {
                initialDocs.add(scoreList.getDocid(i));
            }

            double scale = 1.0;
            if (retrievalAlgorithm.equals("bm25"))
                scale = qidNormalizationScales.get(qid);

            ScoreList diversifiedScoreList = new ScoreList();
            Set<Integer> diversifiedDocIds = new HashSet<>();
            while (!initialDocs.isEmpty()) {
                // find the doc the maximizes (relevance + diversity)
                int chosenDocId = -1;
                double maxScore = -Double.MAX_VALUE;
                for (int i = 0; i < scoreList.size(); ++i) {
                    int docid = scoreList.getDocid(i);
                    if (!initialDocs.contains(docid) || diversifiedDocIds.contains(docid)) continue;

                    // calculate diversity
                    double intentWeight = 1.0 / queryToIntents.get(qid).size(); // uniform intent weight
                    double diversity = 0.0;
                    for (String intentQid : queryToIntents.get(qid)) {
                        ScoreList intentScoreList = initialRanking.get(intentQid);
                        var docid2idx = intentScoreList.getDocidToIndexMapping();

                        int idx = docid2idx.getOrDefault(docid, -1);
                        double s = 0.0;
                        if (idx != -1) s = intentScoreList.getDocidScore(idx) / scale;

                        double prod = 1.0;
                        for (int id : diversifiedDocIds) {
                            idx = docid2idx.getOrDefault(id, -1);
                            if (idx != -1) prod *= 1 - intentScoreList.getDocidScore(idx) / scale;
                        }
                        diversity += intentWeight * s * prod;
                    }

                    // calculate document's score and remember the maximum
                    double relevance = scoreList.getDocidScore(i) / scale;
                    double score = (1 - lambda) * relevance + lambda * diversity;
                    if (score > maxScore) {
                        maxScore = score;
                        chosenDocId = docid;
                    }
                }

                initialDocs.remove(chosenDocId);
                diversifiedScoreList.add(chosenDocId, maxScore);
                diversifiedDocIds.add(chosenDocId);
            }

            diversifiedScoreList.truncate(outputMaxLength);
            res.put(qid, diversifiedScoreList);
        }

        return res;
    }

    public Map<String, ScoreList> PM2(
            Map<String, ScoreList> initialRanking,
            List<String> queries,
            Map<String, List<String>> queryToIntents,
            Map<String, Double> qidNormalizationScales,
            int outputMaxLength,
            double lambda
    ) throws Exception {
        Map<String, ScoreList> res = new LinkedHashMap<>(); // preserve order
        for (String qid : queries) {
            ScoreList scoreList = initialRanking.get(qid);

            // initial set of documents
            Set<Integer> initialDocs = new HashSet<>();
            for (int i = 0; i < scoreList.size(); ++i) {
                initialDocs.add(scoreList.getDocid(i));
            }

            double scale = 1.0;
            if (retrievalAlgorithm.equals("bm25"))
                scale = qidNormalizationScales.get(qid);

            ScoreList diversifiedScoreList = new ScoreList();
            Set<Integer> diversifiedDocIds = new HashSet<>();

            List<String> intents = queryToIntents.get(qid);
            int numIntents = intents.size();

            double[] v = new double[numIntents];
            double[] s = new double[numIntents];
            for (int i = 0; i < numIntents; ++i) {
                v[i] = (double) outputMaxLength / numIntents; // uniform intent weight
                s[i] = 0.0;
            }

            while (!initialDocs.isEmpty()) {
                int tgtIntent = 0;
                double maxQt = -Double.MAX_VALUE;
                double[] qt = new double[numIntents];
                for (int j = 0; j < numIntents; ++j) {
                    qt[j] = v[j] / (2 * s[j] + 1);
                    if (qt[j] > maxQt) {
                        tgtIntent = j;
                        maxQt = qt[j];
                    }
                }

                // Find the doc the maximizes PM2 scores
                int chosenDocId = -1;
                double maxScore = -Double.MAX_VALUE;
                for (int i = 0; i < scoreList.size(); ++i) {
                    int docid = scoreList.getDocid(i);
                    if (!initialDocs.contains(docid) || diversifiedDocIds.contains(docid)) continue;

                    double score1 = 0.0;
                    double score2 = 0.0;
                    for (int intent = 0; intent < numIntents; ++intent) {
                        var scores = initialRanking.get(intents.get(intent));
                        int docIdx = scores.getDocidToIndexMapping().getOrDefault(docid, -1);
                        if (docIdx == -1) continue;

                        if (intent == tgtIntent) {
                            score1 = qt[intent] * scores.getDocidScore(docIdx) / scale;
                        } else {
                            score2 += qt[intent] * scores.getDocidScore(docIdx) / scale;
                        }
                    }

                    double score = lambda * score1 + (1 - lambda) * score2;
                    if (score > maxScore) {
                        maxScore = score;
                        chosenDocId = docid;
                    }
                }

                initialDocs.remove(chosenDocId);
                diversifiedDocIds.add(chosenDocId);

                // make sure score list is monotonically decreasing
                double lastScore = Double.MAX_VALUE;
                if (diversifiedScoreList.size() > 0)
                    lastScore = diversifiedScoreList.getDocidScore(diversifiedScoreList.size() - 1);
                if (maxScore == 0 || maxScore > lastScore) maxScore = 0.999 * lastScore;
                diversifiedScoreList.add(chosenDocId, maxScore);

                for (int j = 0; j < numIntents; ++j) {
                    double numerator = 0.0;
                    double denomenator = 0.0;
                    for (int k = 0; k < numIntents; ++k) {
                        ScoreList scores = initialRanking.get(intents.get(k));
                        int docIdx = scores.getDocidToIndexMapping().getOrDefault(chosenDocId, -1);
                        if (docIdx == -1) continue;

                        double tmp = scores.getDocidScore(docIdx);
                        denomenator += tmp;

                        if (j == k) numerator = tmp;
                    }

                    // division by scale is cancelled
                    if (denomenator != 0.0) s[j] += numerator / denomenator;
                }
            }

            diversifiedScoreList.truncate(outputMaxLength);
            res.put(qid, diversifiedScoreList);
        }

        return res;
    }

    Map<Integer, List<RelevanceLabel>> readRelevanceJudgement(String path) throws IOException {
        var input = new BufferedReader(new FileReader(path));

        Map<Integer, List<RelevanceLabel>> ret = new HashMap<>();
        List<RelevanceLabel> labels = new ArrayList<>();

        int prevQryID = -1;
        String line = null;
        while ((line = input.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            if (tokens.length != 4) continue;

            int qryID = Integer.parseInt(tokens[0]);
            if (qryID != prevQryID) {
                if (prevQryID != -1) ret.put(prevQryID, labels);
                labels = new ArrayList<>();
            }
            prevQryID = qryID;

            String externalID = tokens[2];
            int relevance = Integer.parseInt(tokens[3]);
            if (relevance == -2) relevance = 0;

            labels.add(new RelevanceLabel(qryID, externalID, relevance));
        }

        if (prevQryID != -1) ret.put(prevQryID, labels);
        return ret;
    }

    // Relevance can be an empty map, in this case 0 is used as the relevance of all documents
    Map<Integer, LTRFeatureVectorList> obtainLTRFeatures(
            String qryFile,
            Map<Integer, List<String>> externalIDs,
            Map<Integer, Map<String, Integer>> relevance,
            String disableFeats,
            boolean normalize,
            boolean sort
    ) throws Exception {
        BufferedReader input = null;
        Map<Integer, LTRFeatureVectorList> ret = new LinkedHashMap<>(); // PRESERVE INSERTION ORDER

        HashSet<Integer> disabledFeatIdx = new HashSet<>();
        for (String f : disableFeats.split(",")) {
            if (f.isEmpty()) {
                disabledFeatIdx.addAll(Arrays.asList(16, 17, 18, 19)); // disable custom features by default
            } else {
                disabledFeatIdx.add(Integer.parseInt(f) - 1);
            }
        }

        input = new BufferedReader(new FileReader(qryFile));

        // Each pass of the loop processes one query.
        String qLine = null;
        while ((qLine = input.readLine()) != null) {
            // System.out.println("Generating LTR feature vector for query " + qLine);
            String[] pair = qLine.split(":");
            if (pair.length != 2) {
                throw new IllegalArgumentException("Syntax error:  Each line must contain one ':'.");
            }
            int qid = Integer.parseInt(pair[0].strip());
            String query = pair[1];
            String[] qryTerms = QryParser.tokenizeString(query);

            var bm25 = ((LTR) this.model).getBM25();
            var indri = ((LTR) this.model).getIndri();
            var docs = externalIDs.get(qid);
            if (docs == null) throw new RuntimeException("Cannot find external document IDs for query: " + qid);
            var qryRelevance = relevance.get(qid);

            LTRFeatureVectorList feats = new LTRFeatureVectorList();
            for (String externalID : docs) {
                int docid = Idx.getInternalDocid(externalID);

                Map<Integer, Double> vec = new HashMap<>();
                if (!disabledFeatIdx.contains(0)) vec.put(0, Idx.getDocSpamScore(docid));
                if (!disabledFeatIdx.contains(1)) vec.put(1, Idx.getDocURLDepth(docid));
                if (!disabledFeatIdx.contains(2)) vec.put(2, Idx.getDocWikipediaScore(docid));
                if (!disabledFeatIdx.contains(3)) vec.put(3, Idx.getDocPageRank(docid));

                TermVector termVectorBody = new TermVector(docid, "body");
                double overlap = Utils.getTermOverlap(termVectorBody, qryTerms);
                if (!disabledFeatIdx.contains(4)) vec.put(4, bm25.getScore(qryTerms, termVectorBody, "body"));
                if (!disabledFeatIdx.contains(5)) vec.put(5, indri.getScore(qryTerms, termVectorBody, "body"));
                if (!disabledFeatIdx.contains(6)) vec.put(6, overlap);

                TermVector termVectorTitle = new TermVector(docid, "title");
                overlap = Utils.getTermOverlap(termVectorTitle, qryTerms);
                if (!disabledFeatIdx.contains(7)) vec.put(7, bm25.getScore(qryTerms, termVectorTitle, "title"));
                if (!disabledFeatIdx.contains(8)) vec.put(8, indri.getScore(qryTerms, termVectorTitle, "title"));
                if (!disabledFeatIdx.contains(9)) vec.put(9, overlap);

                TermVector termVectorUrl = new TermVector(docid, "url");
                overlap = Utils.getTermOverlap(termVectorUrl, qryTerms);
                if (!disabledFeatIdx.contains(10)) vec.put(10, bm25.getScore(qryTerms, termVectorUrl, "url"));
                if (!disabledFeatIdx.contains(11)) vec.put(11, indri.getScore(qryTerms, termVectorUrl, "url"));
                if (!disabledFeatIdx.contains(12)) vec.put(12, overlap);

                TermVector termVectorInlink = new TermVector(docid, "inlink");
                overlap = Utils.getTermOverlap(termVectorInlink, qryTerms);
                if (!disabledFeatIdx.contains(13)) vec.put(13, bm25.getScore(qryTerms, termVectorInlink, "inlink"));
                if (!disabledFeatIdx.contains(14)) vec.put(14, indri.getScore(qryTerms, termVectorInlink, "inlink"));
                if (!disabledFeatIdx.contains(15)) vec.put(15, overlap);

                // === Custom features ===
                // body field total length
                if (!disabledFeatIdx.contains(16)) vec.put(16, (double) indri.fieldTotalLengths.get("body"));
                // query length
                if (!disabledFeatIdx.contains(17)) vec.put(17, (double) qryTerms.length);
                // unique terms in document body
                if (!disabledFeatIdx.contains(18)) vec.put(18, (double) termVectorBody.stemsLength());
                // number of inlinks
                if (!disabledFeatIdx.contains(19)) vec.put(19, (double) termVectorInlink.positionsLength());

                int rel = 0;
                if (qryRelevance != null) rel = qryRelevance.getOrDefault(externalID, 0);
                feats.add(qid, docid, externalID, vec, rel);
            }

            if (sort) feats.sort();
            if (normalize) feats.normalize();
            ret.put(qid, feats);
        }

        input.close();
        return ret;
    }

    private void writeLTRFeatures(Map<Integer, LTRFeatureVectorList> feats, String outFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outFile);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        for (var entry : feats.entrySet()) {
            int qid = entry.getKey();
            LTRFeatureVectorList featList = entry.getValue();

            for (int i = 0; i < featList.size(); ++i) {
                writer.write(featList.getRelevance(i) + " qid:" + qid + " ");
                var feat = featList.getDocidFeats(i);

                int prev = 0;
                for (var e : feat.entrySet()) {
                    int idx = e.getKey();

                    // fill in inconsecutive features with 0
                    for (int j = prev + 1; j < idx; ++j) {
                        writer.write((j + 1) + ":0 ");
                    }

                    double val = e.getValue();
                    if (Double.isNaN(val)) val = 0;
                    writer.write((idx + 1) + ":" + val + " ");

                    prev = idx;
                }

                writer.write("# " + featList.getExternalDocid(i) + "\n");
            }
        }

        writer.close();
        fileWriter.close();
    }

    private Qry buildQryObject(String qString) throws IOException {
        String defaultOp = model.defaultQrySopName();
        qString = defaultOp + "(" + qString + ")";
        return QryParser.getQuery(qString);
    }

    private ScoreList evaluateQry(RetrievalModel m, Qry q) throws IOException {
        ScoreList results = new ScoreList();

        if (q.args.size() > 0) {        // Ignore empty queries
            q.initialize(m);

            while (q.docIteratorHasMatch(m)) {
                int docid = q.docIteratorGetMatch();
                double score = ((QrySop) q).getScore(m);
                results.add(docid, score);
                q.docIteratorAdvancePast(docid);
            }
        }

        return results;
    }

    /**
     * Process the query file.
     *
     * @param queryFilePath Path to the query file
     * @throws IOException Error accessing the Lucene index.
     */
    public Map<String, ScoreList> processQueryFile(String queryFilePath) throws IOException {
        BufferedReader input = null;
        Map<String, ScoreList> ret = new LinkedHashMap<>(); // PRESERVE INSERTION ORDER

        try {
            input = new BufferedReader(new FileReader(queryFilePath));

            // Each pass of the loop processes one query.
            Timer timer = new Timer();
            String qLine = null;
            while ((qLine = input.readLine()) != null) {
                printMemoryUsage(false);
                System.out.println("Query " + qLine);
                String[] pair = qLine.split(":");

                if (pair.length != 2) {
                    throw new IllegalArgumentException("Syntax error:  Each line must contain one ':'.");
                }

                String qid = pair[0].strip();
                String query = pair[1];

                timer.start(); // <------

                Qry q = buildQryObject(query);
                System.out.println("    --> " + q);

                ScoreList results = null;
                if (q != null) results = evaluateQry(this.model, q);

                timer.stop(); // <------
                System.out.println("Time used for processing this query: " + timer);

                if (results != null) {
                    results.sort();
                    results.truncate(evalOutputLength);
                    ret.put(qid, results);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            input.close();
        }

        return ret;
    }

    /**
     * Process a query file using PRF
     */
    public Map<String, ScoreList> processQueryFile(
            String queryFilePath,
            Map<String, List<Map.Entry<String, Double>>> prf,
            String expansionField,
            int numTerms,
            double origWeight,
            String expansionQryOutFile
    ) throws IOException {
        BufferedReader input = null;
        Map<String, ScoreList> ret = new LinkedHashMap<>(); // PRESERVE INSERTION ORDER

        FileWriter expansionQryFileWriter = null;
        BufferedWriter expansionQryWriter = null;
        if (!expansionQryOutFile.isEmpty()) {
            expansionQryFileWriter = new FileWriter(expansionQryOutFile);
            expansionQryWriter = new BufferedWriter(expansionQryFileWriter);
        }

        try {
            input = new BufferedReader(new FileReader(queryFilePath));

            // Each pass of the loop processes one query.
            Timer timer = new Timer();
            String qLine = null;
            while ((qLine = input.readLine()) != null) {
                printMemoryUsage(false);
                System.out.println("Query " + qLine);
                String[] pair = qLine.split(":");

                if (pair.length != 2) {
                    throw new IllegalArgumentException("Syntax error:  Each line must contain one ':'.");
                }

                String qid = pair[0].strip();
                String query = pair[1];

                // ===================== Build extended query =====================
                Qry origQry = buildQryObject(query);
                System.out.println("    --> Original query: " + origQry);

                IWeightedSop learnedQry = (IWeightedSop) QryParser.createOperator("#wand");
                String expansionQryOutString = qid + ": #wand ( ";
                int nt = 0;
                List<Map.Entry<String, Double>> qryPrf = prf.get(qid);
                for (Map.Entry<String, Double> termScore : qryPrf) {
                    if (nt >= numTerms) break;

                    String term = termScore.getKey();
                    if (!Utils.isAsciiString(term) || term.contains(".") || term.contains(",")) {
                        continue;
                    }

                    // not entirely accurate but good enough
                    double termWeight = Math.round(termScore.getValue() * 10000.0) / 10000.0;

                    // System.out.println(term + ": " + termWeight);
                    learnedQry.appendWeightedArg(new QryIopTerm(term, expansionField), termWeight);
                    expansionQryOutString += String.format("%.4f", termWeight) + " " +
                            (expansionField.equals("body") ? term : term + "." + expansionField)
                            + " ";

                    ++nt;
                }
                expansionQryOutString += " ) ";
                System.out.println("    --> Learned query: " + learnedQry);

                IWeightedSop expandedQry = (IWeightedSop) QryParser.createOperator("#wand");
                expandedQry.appendWeightedArg(origQry, origWeight);
                expandedQry.appendWeightedArg((Qry) learnedQry, 1 - origWeight);
                System.out.println("    --> Expanded query: " + expandedQry);
                // ================================================================

                // Write learned query to the specified file
                if (expansionQryWriter != null) expansionQryWriter.write(expansionQryOutString + "\n");

                timer.start(); // <------
                ScoreList results = evaluateQry(this.model, (Qry) expandedQry);
                timer.stop(); // <------
                System.out.println("Time used for processing this query: " + timer);

                if (results != null) {
                    results.sort();
                    ret.put(qid, results);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            input.close();
            if (expansionQryWriter != null) expansionQryWriter.close();
            if (expansionQryFileWriter != null) expansionQryFileWriter.close();
        }

        return ret;
    }

    /**
     * Initialize PRF scores from existing ranking results
     *
     * @param initialRanking Initial ranking
     * @param numDocs        The number of documents to use for query expansion.
     * @return Term scores used for building extended queries
     */
    public Map<String, List<Map.Entry<String, Double>>> initializePrf(
            Map<String, ScoreList> initialRanking,
            String expansionField,
            int numDocs,
            double mu
    ) throws Exception {
        Map<String, List<Map.Entry<String, Double>>> ret = new HashMap<>();
        Timer timer = new Timer();
        timer.start();

        long L = ((Indri) model).fieldTotalLengths.get(expansionField);

        // for each query
        for (Map.Entry<String, ScoreList> e : initialRanking.entrySet()) {
            int docs = 0;
            double prevDocIncre = 0.0; // https://boston.lti.cs.cmu.edu/classes/11-642/HW/HW3/FAQ.html
            String qid = e.getKey();
            ScoreList scoreList = e.getValue();
            Map<String, Double> termScores = new HashMap<>();

            // for each doc of this query
            for (int r = 0; r < scoreList.size(); ++r) {
                if (docs >= numDocs) continue;

                TermVector tv = new TermVector(scoreList.getDocid(r), expansionField);
                double docLen = tv.positionsLength();

                // terms in this document
                for (int i = 1; i < tv.stemsLength(); ++i) {
                    assert docLen > 0;

                    double ctf = tv.totalStemFreq(i);
                    double p_td = (
                            tv.stemFreq(i) + mu * ctf / L
                    ) / (docLen + mu);
                    double idf = Math.log(L) - Math.log(ctf);
                    double score = p_td * idf * scoreList.getDocidScore(r);

                    String stem = tv.stemString(i);
                    if (termScores.containsKey(stem)) {
                        termScores.put(stem, termScores.get(stem) + score);
                    } else { // first encounter of this term
                        termScores.put(stem, prevDocIncre * ctf / L * idf + score);
                    }
                }

                // terms that are missing in this document but present in other docs
                for (String stem : termScores.keySet()) {
                    if (tv.indexOfStem(stem) != -1) continue;

                    double ctf = Idx.getTotalTermFreq(expansionField, stem);
                    double idf = Math.log(L) - Math.log(ctf);
                    double p_td = (
                            0.0 + mu * ctf / L
                    ) / (docLen + mu);
                    if (Double.isNaN(p_td)) p_td = 0.0; // essentially skip this doc when docLen and mu are both 0
                    termScores.put(stem, termScores.get(stem) + p_td * idf * scoreList.getDocidScore(r));
                }

                if (docLen + mu != 0) // essentially skip this doc when docLen and mu are both 0
                    prevDocIncre += mu / (docLen + mu) * scoreList.getDocidScore(r);
                ++docs;
            }

            // finish loading this query and reset
            // sort by scores descendingly
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(termScores.entrySet());
            // sort by score, if score is the same sort by term string
            sorted.sort(
                    (e1, e2) -> e2.getValue().equals(e1.getValue()) ?
                            e1.getKey().compareTo(e2.getKey())
                            : e2.getValue().compareTo(e1.getValue())
            );
            ret.put(qid, sorted);
        }

        timer.stop(); // <------
        System.out.println("Time used for building extended query: " + timer);
        return ret;
    }

    /**
     * Print a message indicating the amount of memory used. The caller can
     * indicate whether garbage collection should be performed, which slows the
     * program but reduces memory usage.
     *
     * @param gc If true, run the garbage collector before reporting.
     */
    public void printMemoryUsage(boolean gc) {
        Runtime runtime = Runtime.getRuntime();

        if (gc)
            runtime.gc();

        System.out.println("Memory used:  "
                + ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)) + " MB");
    }

}
