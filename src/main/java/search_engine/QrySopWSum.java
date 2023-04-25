/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;

/**
 * The WSUM operator for Indri model.
 */
public class QrySopWSum extends QrySopWeighted implements IIndriSop {

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
        if (r instanceof Indri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException(r.getClass().getName() + " doesn't support the WSUM operator.");
        }
    }

    /**
     * getScore for Indri model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreIndri(RetrievalModel r) throws IOException {
        double score = 0;
        int docid = this.docIteratorGetMatch();

        for (int i = 0; i < this.args.size(); ++i) {
            QrySop q = (QrySop) this.args.get(i);
            if (q.docIteratorHasMatch(r) && (q.docIteratorGetMatch() == docid)) {
                score += this.weights.get(i) * q.getScore(r);
            } else {
                score += this.weights.get(i) * ((IIndriSop) q).getDefaultScore((Indri) r, docid);
            }
        }
        return score / weightSum;
    }

    @Override
    public double getDefaultScore(Indri r, int docid) throws IOException {
        double score = 0;

        for (int i = 0; i < this.args.size(); ++i) {
            QrySop q = (QrySop) this.args.get(i);
            double argWeight = this.weights.get(i) / weightSum;
            score += argWeight * ((IIndriSop) q).getDefaultScore(r, docid);
        }
        return score;
    }
}
