package search_engine;

import java.io.IOException;

public class LTR extends CachedRetrievalModel {
    Indri indri;
    BM25 bm25;

    public LTR(Indri indri, BM25 bm25) throws IOException {
        super();
        this.indri = indri;
        this.bm25 = bm25;
    }

    public BM25 getBM25() {
        return bm25;
    }

    public Indri getIndri() {
        return indri;
    }

    @Override
    public String defaultQrySopName() {
        return bm25.defaultQrySopName(); // uses bm25 to create initial ranking
    }
}
