package search_engine;

import java.util.*;

public class LTRFeatureVectorList {

    public class LTRFeatureVectorEntry {
        public int docid;
        public String externalId;
        public Map<Integer, Double> featVector;
        public int qryID;
        public int relevance;

        private LTRFeatureVectorEntry(
                int qryID,
                int internalDocid,
                String externalId,
                Map<Integer, Double> featVector,
                int relevance
        ) {
            this.qryID = qryID;
            this.docid = internalDocid;
            this.externalId = externalId;
            this.featVector = featVector;
            this.relevance = relevance;
        }
    }

    /**
     * A list of document ids and features.
     */
    private List<LTRFeatureVectorEntry> feats = new ArrayList<>();

    /**
     * Normalize feature vectors in this list across entries.
     */
    public void normalize() {
        int size = size();
        assert size > 0;
        int qid = get(0).qryID;

        Map<Integer, Double> minVals = new HashMap<>();
        Map<Integer, Double> maxVals = new HashMap<>();
        for (int i = 0; i < size; ++i) {
            assert qid == get(i).qryID;

            var vec = get(i).featVector;
            for (int j : vec.keySet()) {
                double val = vec.get(j);
                if (Double.isNaN(val)) continue;

                if (!minVals.containsKey(j)) {
                    minVals.put(j, val);
                    maxVals.put(j, val);
                } else {
                    minVals.put(j, Math.min(minVals.get(j), val));
                    maxVals.put(j, Math.max(maxVals.get(j), val));
                }
            }
        }

        for (int i = 0; i < size; ++i) {
            var vec = get(i).featVector;
            for (int j : vec.keySet()) {
                double min = minVals.get(j);
                double max = maxVals.get(j);
                double val = vec.get(j);

                if (max == min || Double.isNaN(val))
                    vec.put(j, 0.0);
                else
                    vec.put(j, (val - min) / (max - min));
            }
        }
    }

    /**
     * Append a document feature to a feature list.
     */
    public void add(
            int qryID,
            int docid,
            String externalDocID,
            Map<Integer, Double> feature,
            int relevance
    ) throws Exception {
        feats.add(new LTRFeatureVectorEntry(qryID, docid, externalDocID, feature, relevance));
    }

    /**
     * Get the external docid of the n'th entry.
     *
     * @param n The index of the requested document.
     * @return The internal document id.
     */
    public String getExternalDocid(int n) {
        return this.feats.get(n).externalId;
    }

    public int getRelevance(int n) {
        return this.feats.get(n).relevance;
    }

    /**
     * Get the feature of the n'th entry.
     *
     * @param n The index of the requested document feature.
     * @return The document's feature.
     */
    public Map<Integer, Double> getDocidFeats(int n) {
        return this.feats.get(n).featVector;
    }

    public LTRFeatureVectorEntry get(int n) {
        return this.feats.get(n);
    }

    /**
     * Get the size of the feature list.
     *
     * @return The size of the posting list.
     */
    public int size() {
        return this.feats.size();
    }

    /**
     * Compare two LTRFeatureVectorEntry objects. Sort by qryID, then external docid.
     */
    public class ScoreListComparator implements Comparator<LTRFeatureVectorEntry> {
        @Override
        public int compare(LTRFeatureVectorEntry s1, LTRFeatureVectorEntry s2) {
            if (s1.qryID < s2.qryID)
                return -1;
            else if (s1.qryID > s2.qryID)
                return 1;
            return s1.externalId.compareTo(s2.externalId); // lexicographical comparison of external id
        }
    }

    /**
     * Sort the list by external document id.
     */
    public void sort() {
        this.feats.sort(new ScoreListComparator());
    }
}
