/*
 *  Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 *  Version 3.11.
 *
 *  Compatible with Lucene 8.1.1.
 */
package search_engine;

import java.io.*;
import java.util.*;

/**
 * This software illustrates the architecture for the portion of a
 * search engine that evaluates queries.  It is a guide for class
 * homework assignments, so it emphasizes simplicity over efficiency.
 * It implements an unranked Boolean retrieval model, however it is
 * easily extended to other retrieval models.  For more information,
 * see the ReadMe.txt file.
 */
public class QryEval {

    private static final String USAGE = "Usage:  java QryEval paramFile\n\n";

    /**
     * @param args The only argument is the parameter file name.
     * @throws Exception Error accessing the Lucene index.
     */
    public static void main(String[] args) throws Exception {
        Timer timer = new Timer();
        timer.start();

        // Check that a parameter file is included
        if (args.length < 1) {
            throw new IllegalArgumentException(USAGE);
        }
        Map<String, String> parameters = readParameterFile(args[0]);

        // Process queries
        SearchEngine se = new SearchEngine(parameters);
        Map<String, ScoreList> res = se.run();

        // Write results to file
        writeResults(res, parameters.get("trecEvalOutputPath"), Integer.parseInt(parameters.get("trecEvalOutputLength")));

        // Clean up
        timer.stop();
        System.out.println("Total running time:  " + timer);
    }

    /**
     * Write the query results to a file.
     * <p>
     * Output format:
     * <p>
     * QueryID Q0 DocID Rank Score RunID
     *
     * @param result         A Map containing query ID (String) and its query result (ScoreList)
     * @param outputFilePath Output file path
     * @param maxNumRes      Maximum number of lines written per query
     * @throws IOException Error accessing the Lucene index.
     */
    public static void writeResults(
            Map<String, ScoreList> result,
            String outputFilePath,
            int maxNumRes
    ) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFilePath);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        for (Map.Entry<String, ScoreList> e : result.entrySet()) {
            String qid = e.getKey();
            ScoreList r = e.getValue();

            if (r.size() < 1) { // no document retrieved
                writer.write(qid + " Q0 dummy 1 0 reference\n");
            } else {
                for (int i = 0; i < Math.min(r.size(), maxNumRes); i++) {
                    int rank = i + 1;
                    writer.write(
                            qid + " Q0 " + Idx.getExternalDocid(r.getDocid(i))
                                    + " " + rank + " " + String.format("%.12f", r.getDocidScore(i)) + " reference\n"
                    );
                }
            }
        }

        writer.close();
        fileWriter.close();
    }

    /**
     * Read the specified parameter file, and confirm that the required
     * parameters are present.  The parameters are returned in a
     * HashMap.  The caller (or its minions) are responsible for processing
     * them.
     *
     * @return The parameters, in <key, value> format.
     */
    private static Map<String, String> readParameterFile(String parameterFileName) throws IOException {
        Map<String, String> parameters = new HashMap<String, String>();
        File parameterFile = new File(parameterFileName);

        if (!parameterFile.canRead()) {
            throw new IllegalArgumentException("Can't read " + parameterFileName);
        }

        //  Store (all) key/value parameters in a hashmap.

        Scanner scan = new Scanner(parameterFile);
        String line = null;
        do {
            line = scan.nextLine();
            String[] pair = line.split("=");
            parameters.put(pair[0].trim(), pair[1].trim());
        } while (scan.hasNext());

        scan.close();

        //  Confirm that some of the essential parameters are present.
        //  This list is not complete.  It is just intended to catch silly
        //  errors.
        if (!(parameters.containsKey("indexPath") && parameters.containsKey("queryFilePath") && parameters.containsKey("trecEvalOutputPath") && parameters.containsKey("retrievalAlgorithm"))) {
            throw new IllegalArgumentException("Required parameters were missing from the parameter file.");
        }

        return parameters;
    }
}
