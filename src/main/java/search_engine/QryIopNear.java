package search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NEAR/n: return a document if all of the query arguments occur in the document, <b>in order</b>, with no more than
 * n-1 terms separating two adjacent terms.
 * <p>
 * For example, #NEAR/2(a b c) matches "a b c", "a x b c", "a b x c", and "a x b x c", but not "a x x b c".
 * The document's score will be the number of times the NEAR/n operator matched the document (i.e., its frequency).
 * </p>
 */
public class QryIopNear extends QryIop {
    private int maxDistance = 0;

    public QryIopNear(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    /**
     * Evaluate the query operator; the result is an internal inverted
     * list that may be accessed via the internal iterators.
     *
     * @throws IOException Error accessing the Lucene index.
     */
    protected void evaluate() throws IOException {
        this.invertedList = new InvList(this.getField());
        if (args.size() == 0) return;

        while (this.docIteratorHasMatchAll(null)) { // iterate through all documents that contains all query args
            // DON'T use this.docIteratorGetMatch() because it uses the empty inverted list we are filling in right now
            int docid = args.get(0).docIteratorGetMatch();
            if (docid == Qry.INVALID_DOCID) break;

            // Find occurrences of the NEAR/n query
            List<Integer> positions = new ArrayList<>();
            while (true) {
                // make sure positions of args are minimum and monotonically increasing
                boolean done = false;
                for (int i = 0; i < this.args.size(); ++i) {
                    QryIop q = (QryIop) this.args.get(i);
                    if (!q.locIteratorHasMatch()) {
                        done = true;
                        break;
                    }

                    if (i > 0) {
                        QryIop prevQ = (QryIop) this.args.get(i - 1);
                        q.locIteratorAdvancePast(prevQ.locIteratorGetMatch());
                        if (!q.locIteratorHasMatch()) {
                            done = true;
                            break;
                        }
                    }
                }
                if (done) break;

                // check distance between each consecutive query args
                boolean near = true;
                for (int i = 1; i < this.args.size(); ++i) {
                    QryIop q = (QryIop) this.args.get(i);
                    QryIop prevQ = (QryIop) this.args.get(i - 1);
                    int locA = prevQ.locIteratorGetMatch();
                    int locB = q.locIteratorGetMatch();

                    assert locA < locB;
                    if (locB - locA > maxDistance) {
                        near = false;
                        break;
                    }
                }

                // add to inverted list if near
                if (near) {
                    positions.add(((QryIop) this.args.get(this.args.size() - 1)).locIteratorGetMatch());

                    // consume all indices
                    for (Qry arg : this.args) {
                        ((QryIop) arg).locIteratorAdvance();
                    }
                } else {
                    // consume the first query arg index
                    ((QryIop) this.args.get(0)).locIteratorAdvance();
                }
            }

            // sort and add to inverted list
            if (positions.size() > 0) {
                Collections.sort(positions);
                this.invertedList.appendPosting(docid, positions);
            }

            // advance doc iterators
            for (Qry q : this.args) {
                q.docIteratorAdvancePast(docid);
            }
        }
    }

}
