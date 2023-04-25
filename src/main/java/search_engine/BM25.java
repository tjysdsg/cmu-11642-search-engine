package search_engine;

import java.io.IOException;

public class BM25 extends CachedRetrievalModel {
    private double b;
    private double k1;
    private double k3;

    public double getB() {
        return b;
    }

    public double getK1() {
        return k1;
    }

    public double getK3() {
        return k3;
    }

    public BM25(double b, double k1, double k3) throws IOException {
        super();
        this.b = b;
        this.k1 = k1;
        this.k3 = k3;
    }

    @Override
    public String defaultQrySopName() {
        return "#sum";
    }

    /**
     * getScore for BM25 model per term per document.
     */
    public double getScore(String stem, TermVector termVector, String field) throws IOException {
        long docLen = Idx.getFieldLength(field, termVector.docId);
        int stemIdx = termVector.indexOfStem(stem);
        if (stemIdx == -1) return 0;

        int tf = termVector.stemFreq(stemIdx);
        long N = Idx.getNumDocs();
        double avgDocLen = fieldTotalLengths.get(field) / (double) fieldDocCounts.get(field);
        int df = termVector.stemDf(stemIdx);

        double rsjWeight = Math.max(0, Math.log((N - df + 0.5) / (df + 0.5)));
        double termWeight = tf / (
                tf + getK1() * (1 - getB() + getB() * docLen / avgDocLen)
        );
        return rsjWeight * termWeight;
    }

    /**
     * getScore for BM25 model of a bag of words per document.
     */
    public double getScore(String[] stems, TermVector termVector, String field) throws IOException {
        double score = 0;

        if (termVector.positionsLength() == 0 && termVector.stemsLength() == 0) return Double.NaN;
        for (String stem : stems) {
            score += getScore(stem, termVector, field); // user weight is 1 for #SUM
        }

        return score;
    }
}
