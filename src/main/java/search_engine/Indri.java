package search_engine;

import java.io.IOException;

public class Indri extends CachedRetrievalModel {
    private double mu;
    private double lambda;

    public double getMu() {
        return mu;
    }

    public double getLambda() {
        return lambda;
    }

    public Indri(double mu, double lambda) throws IOException {
        super();
        this.mu = mu;
        this.lambda = lambda;
    }

    @Override
    public String defaultQrySopName() {
        return "#and";
    }

    public double getDefaultScore(String stem, TermVector termVector, String field) throws IOException {
        double ctf = Idx.getTotalTermFreq(field, stem);
        if (ctf == 0) ctf = 0.5;

        double pMLE = ctf / fieldTotalLengths.get(field);
        double pDirichlet = (
                0 + getMu() * pMLE
        ) / (
                Idx.getFieldLength(field, termVector.docId) /* faster than TermVector because cached */ + getMu()
        );
        if (Double.isNaN(pDirichlet)) pDirichlet = 0.0; // in case both mu and docLen are 0

        return (1 - getLambda()) * pDirichlet + getLambda() * pMLE;
    }

    public double getScore(int stemIdx, TermVector termVector, String field) throws IOException {
        double pMLE = (double) termVector.totalStemFreq(stemIdx) / fieldTotalLengths.get(field);
        double pDirichlet = (
                termVector.stemFreq(stemIdx) + getMu() * pMLE
        ) / (
                Idx.getFieldLength(field, termVector.docId) /* faster than TermVector because cached */ + getMu()
        );
        if (Double.isNaN(pDirichlet)) pDirichlet = 0.0; // in case both mu and docLen are 0

        return (1 - getLambda()) * pDirichlet + getLambda() * pMLE;
    }

    /**
     * getScore for Indri model of a bag of words per document.
     * NOTE: The result will be incorrect if there is no overlapping between stems and termVector.
     */
    public double getScore(String[] stems, TermVector termVector, String field) throws IOException {
        double score = 0.0;

        if (termVector.positionsLength() == 0 && termVector.stemsLength() == 0) return Double.NaN;
        int nOverlap = 0;
        for (String stem : stems) {
            int stemIdx = termVector.indexOfStem(stem);

            double qScore;
            if (stemIdx == -1) {
                qScore = getDefaultScore(stem, termVector, field);
            } else {
                qScore = getScore(stemIdx, termVector, field);
                ++nOverlap;
            }

            score += Math.log(qScore);  // avoid precision problem
        }

        if (nOverlap == 0) return 0;
        return Math.exp(score / stems.length);
    }
}
