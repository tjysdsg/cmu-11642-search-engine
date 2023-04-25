/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;

/**
 * The WAND operator for all retrieval models.
 */
public class QrySopWAnd extends QrySopWeighted implements IIndriSop {

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
            throw new IllegalArgumentException(r.getClass().getName() + " doesn't support the WAND operator.");
        }
    }

    private double getScoreIndri(RetrievalModel r) throws IOException {
        int docid = this.docIteratorGetMatch();

        double score = 0;
        for (int i = 0; i < this.args.size(); ++i) {
            QrySop q = (QrySop) this.args.get(i);
            double qScore;
            if (q.docIteratorHasMatch(r) && q.docIteratorGetMatch() == docid) {
                qScore = q.getScore(r);
            } else {
                qScore = ((IIndriSop) q).getDefaultScore((Indri) r, docid);
            }

            score += this.weights.get(i) * Math.log(qScore);
        }

        return Math.exp(score / weightSum);
    }

    public double getDefaultScore(Indri r, int docid) throws IOException {
        double score = 0;
        for (int i = 0; i < this.args.size(); ++i) {
            QrySop q = (QrySop) this.args.get(i);
            score += this.weights.get(i) * Math.log(
                    ((IIndriSop) q).getDefaultScore(r, docid)
            );
        }
        return Math.exp(score / weightSum);
    }
}
