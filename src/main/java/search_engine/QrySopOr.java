/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;

/**
 * The OR operator for all retrieval models.
 */
public class QrySopOr extends QrySop implements IIndriSop {

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
        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean(r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean(r);
        } else if (r instanceof Indri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException(r.getClass().getName() + " doesn't support the OR operator.");
        }
    }

    private double getScoreIndri(RetrievalModel r) throws IOException {
        int docid = this.docIteratorGetMatch();

        double score = 0;
        for (Qry arg : this.args) {
            QrySop q = (QrySop) arg;
            double qScore;
            if (q.docIteratorHasMatch(r) && q.docIteratorGetMatch() == docid) {
                qScore = q.getScore(r);
            } else {
                qScore = ((IIndriSop) q).getDefaultScore((Indri) r, docid);
            }

            score += Math.log(1 - qScore);  // avoid precision problem
        }

        return 1 - Math.exp(score / this.args.size());
    }

    @Override
    public double getDefaultScore(Indri r, int docid) throws IOException {
        double pow = 1.0 / this.args.size();
        double score = 0;
        for (Qry arg : this.args) {
            QrySop q = (QrySop) arg;
            score *= Math.pow(1 - ((IIndriSop) q).getDefaultScore(r, docid), pow);
        }
        return 1 - score;
    }

    /**
     * getScore for the UnrankedBoolean retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        //  Unranked Boolean systems only have two scores:
        //  1 (document matches) and 0 (document doesn't match).  QryEval
        //  only calls getScore for documents that match, so if we get
        //  here, the document matches, and its score should be 1.  The
        //  most efficient implementation returns 1 from here.
        return 1;
    }

    /**
     * getScore for the RankedBoolean retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        double score = 0.0;
        int docid = this.docIteratorGetMatch();

        for (int i = 0; i < this.args.size(); i++) {
            QrySop q_i = (QrySop) this.args.get(i);

            if (q_i.docIteratorHasMatch(r) && (q_i.docIteratorGetMatch() == docid)) {
                score = Math.max(score, q_i.getScore(r));
            }
        }

        return score;
    }
}
