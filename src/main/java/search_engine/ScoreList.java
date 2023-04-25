/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;
import java.util.*;

/**
 * This class implements the document score list data structure
 * and provides methods for accessing and manipulating them.
 */
public class ScoreList {

    //  A utility class to create a <internalDocid, externalDocid, score>
    //  object.

    private class ScoreListEntry {
        private int docid;
        private String externalId;
        private double score;

        private ScoreListEntry(int internalDocid, String externalId, double score) {
            this.docid = internalDocid;
            this.externalId = externalId;
            this.score = score;
        }
    }

    /**
     * A list of document ids and scores.
     */
    private List<ScoreListEntry> scores = new ArrayList<ScoreListEntry>();

    public Map<Integer, Integer> getDocidToIndexMapping() {
        Map<Integer, Integer> ret = new HashMap<>();
        for (int i = 0; i < scores.size(); ++i) {
            ret.put(scores.get(i).docid, i);
        }
        return ret;
    }

    /**
     * Append a document score to a score list.
     *
     * @param docid An internal document id.
     * @param score The document's score.
     */
    public void add(int docid, double score) throws IOException {
        scores.add(new ScoreListEntry(docid, Idx.getExternalDocid(docid), score));
    }

    /**
     * Append a document score to a score list.
     *
     * @param externalDocID An external document id.
     * @param score         The document's score.
     */
    public void add(String externalDocID, double score) throws Exception {
        int docid = Idx.getInternalDocid(externalDocID);
        scores.add(new ScoreListEntry(docid, externalDocID, score));
    }

    /**
     * Get the internal docid of the n'th entry.
     *
     * @param n The index of the requested document.
     * @return The internal document id.
     */
    public int getDocid(int n) {
        return this.scores.get(n).docid;
    }

    /**
     * Get the external docid of the n'th entry.
     *
     * @param n The index of the requested document.
     * @return The external document id.
     */
    public String getExternalDocid(int n) {
        return this.scores.get(n).externalId;
    }

    /**
     * Get the score of the n'th entry.
     *
     * @param n The index of the requested document score.
     * @return The document's score.
     */
    public double getDocidScore(int n) {
        assert n >= 0 && n < this.scores.size();
        return this.scores.get(n).score;
    }

    /**
     * Set the score of the n'th entry.
     *
     * @param n     The index of the score to change.
     * @param score The new score.
     */
    public void setDocidScore(int n, double score) {
        this.scores.get(n).score = score;
    }

    /**
     * Get the size of the score list.
     *
     * @return The size of the posting list.
     */
    public int size() {
        return this.scores.size();
    }

    /**
     * Compare two ScoreListEntry objects. Sort by score, then external docid.
     */
    public class ScoreListComparator implements Comparator<ScoreListEntry> {

        @Override
        public int compare(ScoreListEntry s1, ScoreListEntry s2) {
            if (s1.score > s2.score)
                return -1;
            else if (s1.score < s2.score)
                return 1;
            else return s1.externalId.compareTo(s2.externalId); // lexicographical comparison of external id
        }
    }

    /**
     * Sort the list by score and external document id.
     */
    public void sort() {
        Collections.sort(this.scores, new ScoreListComparator());
    }

    /**
     * Reduce the score list to the first num results to save on RAM.
     *
     * @param num Number of results to keep.
     */
    public void truncate(int num) {
        List<ScoreListEntry> truncated = new ArrayList<ScoreListEntry>(this.scores.subList(0,
                Math.min(num, scores.size())));
        this.scores.clear();
        this.scores = truncated;
    }

    public double sumScores() {
        double sum = 0.0;
        for (ScoreListEntry entry : scores) sum += entry.score;
        return sum;
    }
}
