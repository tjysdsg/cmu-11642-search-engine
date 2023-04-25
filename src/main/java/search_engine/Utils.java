/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

/**
 * Miscellaneous utilities.
 */
public class Utils {

    // Allocating a CharsetEncoder is a little slow, so do it just once.
    private static final CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();

    /**
     * Run an external process.
     *
     * @param processName a name to display if the process fails
     * @param parameters  an array of strings used to construct the commandline
     * @throws Exception the external process failed.
     */
    public static void runExternalProcess(String processName, String[] parameters)
            throws Exception {

        Process cmdProc = Runtime.getRuntime().exec(parameters);

        // Consume stdout and stderr.  THIS IS REQUIRED, otherwise the
        // buffers may fill, causing the program to stall.  Echoing
        // stdout and saving stderr (in case an exception is necessary)
        // is optional.
        var stdoutStream = new ReadStream(cmdProc.getInputStream());
        var stderrStream = new ReadStream(cmdProc.getErrorStream());
        stdoutStream.start();
        stderrStream.start();

        // Throw an exception if the process has problems.
        int retValue = cmdProc.waitFor();
        if (retValue != 0) {
            throw new Exception(processName + " crashed");
        }
    }

    public static Map<String, ScoreList> readRankingFileAsScoreList(String path) throws Exception {
        Map<String, ScoreList> ret = new LinkedHashMap<>(); // preserve order
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                String[] substrings = line.split("\\s+");
                String qryID = substrings[0];
                String externalDocID = substrings[2];
                double score = Double.parseDouble(substrings[4]);

                ret.computeIfAbsent(qryID, k -> new ScoreList()).add(externalDocID, score);
            }
        }

        return ret;
    }

    public static List<RankEntry> readRankingFile(String path) throws IOException {
        List<RankEntry> ret = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                String[] substrings = line.split("\\s+");
                String qryID = substrings[0];
                String docID = substrings[2];
                int rank = Integer.parseInt(substrings[3]);
                double score = Double.parseDouble(substrings[4]);

                ret.add(new RankEntry(qryID, docID, rank, score));
            }
        }

        return ret;
    }

    /**
     * Tells whether a string contains only ASCII characters.
     *
     * @param s The string to check.
     * @return True if, and only if, s is entirely ASCII.
     */
    public static boolean isAsciiString(String s) {
        return asciiEncoder.canEncode(s);
    }

    public static double getTermOverlap(TermVector termVector, String[] stems) {
        double ret = 0;
        if (termVector.stemsLength() == 0 && termVector.positionsLength() == 0) return Double.NaN;
        for (String stem : stems) {
            int stemIdx = termVector.indexOfStem(stem);
            if (stemIdx != -1) ++ret;
        }

        return ret;
    }
}

class ReadStream implements Runnable {
    InputStream is;
    Thread thread;

    public ReadStream(InputStream is) {
        this.is = is;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}