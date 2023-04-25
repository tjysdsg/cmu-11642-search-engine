/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

import java.io.IOException;

/**
 * The SCORE operator for all retrieval models.
 */
public class QrySopScore extends QrySop implements IIndriSop {

    /**
     * Indicates whether the query has a match.
     *
     * @param r The retrieval model that determines what is a match
     * @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchFirst(r);
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
        } else if (r instanceof BM25) {
            return this.getScoreBM25(r);
        } else if (r instanceof LTR) {
            return this.getScoreBM25(((LTR) r).getBM25());
        } else if (r instanceof Indri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException(r.getClass().getName() + " doesn't support the SCORE operator.");
        }
    }

    /**
     * getScore for the Unranked retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        //  Unranked Boolean systems return 1 for all matches.
        //
        //  Other retrieval models must do more work.  To help students
        //  understand how to implement other retrieval models, this
        //  method does a little more work.

        return 1.0;
    }

    /**
     * getScore for the Ranked retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        QryIop q = (QryIop) this.args.get(0);
        if (q.docIteratorHasMatch(r)) {
            return q.docIteratorGetMatchPosting().tf; // term frequency
        }

        return 0;
    }

    /**
     * getScore for BM25 model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreBM25(RetrievalModel r) throws IOException {
        assert this.docIteratorHasMatch(r);

        BM25 model = (BM25) r;
        QryIop q = (QryIop) this.args.get(0);

        long docLen = Idx.getFieldLength(q.field, q.docIteratorGetMatch());
        int tf = q.docIteratorGetMatchPosting().tf;
        long N = Idx.getNumDocs();
        double avgDocLen = model.fieldTotalLengths.get(q.field) / (double) model.fieldDocCounts.get(q.field);
        int df = q.getDf();

        double rsjWeight = Math.max(0, Math.log((N - df + 0.5) / (df + 0.5)));
        double termWeight = tf / (
                tf + model.getK1() * (
                        1 - model.getB() + model.getB() * docLen / avgDocLen
                )
        );
        return rsjWeight * termWeight;
    }

    /**
     * getScore for Indri model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreIndri(RetrievalModel r) throws IOException {
        assert this.docIteratorHasMatch(r);

        Indri model = (Indri) r;
        QryIop q = (QryIop) this.args.get(0);

        double pMLE = (double) q.getCtf() / model.fieldTotalLengths.get(q.field);
        double pDirichlet = (
                q.docIteratorGetMatchPosting().tf + model.getMu() * pMLE
        ) / (
                Idx.getFieldLength(q.field, q.docIteratorGetMatch()) + model.getMu()
        );
        if (Double.isNaN(pDirichlet)) pDirichlet = 0.0; // in case both mu and docLen are 0

        return (1 - model.getLambda()) * pDirichlet + model.getLambda() * pMLE;
    }

    /**
     * getDefaultScore for Indri model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    @Override
    public double getDefaultScore(Indri r, int docid) throws IOException {
        QryIop q = (QryIop) this.args.get(0);

        double ctf = q.getCtf();
        if (ctf == 0) ctf = 0.5;

        double pMLE = ctf / r.fieldTotalLengths.get(q.field);
        double pDirichlet = (
                0 + r.getMu() * pMLE
        ) / (
                Idx.getFieldLength(q.field, docid) + r.getMu()
        );
        if (Double.isNaN(pDirichlet)) pDirichlet = 0.0; // in case both mu and docLen are 0

        return (1 - r.getLambda()) * pDirichlet + r.getLambda() * pMLE;
    }

    /**
     * Initialize the query operator (and its arguments), including any
     * internal iterators.  If the query operator is of type QryIop, it
     * is fully evaluated, and the results are stored in an internal
     * inverted list that may be accessed via the internal iterator.
     *
     * @param r A retrieval model that guides initialization
     * @throws IOException Error accessing the Lucene index.
     */
    public void initialize(RetrievalModel r) throws IOException {
        Qry q = this.args.get(0);
        q.initialize(r);
    }

}
