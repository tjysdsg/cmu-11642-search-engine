package search_engine;

import java.io.IOException;

public interface IIndriSop {
    /**
     * Get a default score for the document that docIteratorHasMatch didn't match.
     *
     * @param r     The retrieval model that determines how scores are calculated.
     * @param docid Internal Document ID
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    double getDefaultScore(Indri r, int docid) throws IOException;
}
