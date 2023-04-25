/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;

/**
 * The SUM operator for BM25 and Indri model.
 */
public class QrySopSum extends QrySop {
    /**
     * Indicates whether the query has a match.
     *
     * @param r The retrieval model that determines what is a match
     * @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }

    /**
     * Get a score for the document that docIteratorHasMatch matched.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof BM25) {
            return this.getScoreBM25(r);
        } else if (r instanceof LTR) {
            return this.getScoreBM25(((LTR) r).getBM25());
        } else {
            throw new IllegalArgumentException(r.getClass().getName() + " doesn't support the SUM operator.");
        }
    }

    /**
     * getScore for BM25 model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreBM25(RetrievalModel r) throws IOException {
        double score = 0;
        int docid = this.docIteratorGetMatch();

        for (Qry _q : this.args) {
            QrySop q = (QrySop) _q;
            if (q.docIteratorHasMatch(r) && (q.docIteratorGetMatch() == docid)) {
                score += q.getScore(r); // user weight is 1 for #SUM
            }
        }
        return score;
    }
}
