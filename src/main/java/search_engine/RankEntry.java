package search_engine;

/**
 * Stores information about one of the documents in the ranking result
 */
public class RankEntry {
    public String qryID;
    public String docID;
    public int rank;
    public double score;

    public RankEntry(String qryID, String docID, int rank, double score) {
        this.qryID = qryID;
        this.docID = docID;
        this.rank = rank;
        this.score = score;
    }
}
