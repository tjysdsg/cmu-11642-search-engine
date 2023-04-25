package search_engine;

/**
 * Relevance judgement information for a document
 */
public class RelevanceLabel {
    public int qryID;
    public String externalID;
    public int relevance;

    public RelevanceLabel(int qryID, String externalID, int relevance) {
        this.qryID = qryID;
        this.externalID = externalID;
        this.relevance = relevance;
    }
}
