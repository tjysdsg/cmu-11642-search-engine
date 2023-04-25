package search_engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RetrievalModel that needs statistics from Idx, therefore these stats are cached during construction
 */
public abstract class CachedRetrievalModel extends RetrievalModel {
    public Map<String, Long> fieldTotalLengths = new HashMap<>();
    public Map<String, Long> fieldDocCounts = new HashMap<>();

    public CachedRetrievalModel() throws IOException {
        for (String f : Idx.ALL_FIELDS) {
            fieldTotalLengths.put(f, Idx.getSumOfFieldLengths(f));
            fieldDocCounts.put(f, (long) Idx.getDocCount(f));
        }
    }
}
