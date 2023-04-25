package search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WINDOW/n: similar to NEAR/n, but the order of words is arbitrary, and n refers to the max distance between any
 * pairs of words
 */
public class QryIopWindow extends QryIop {
    private int maxDistance = 0;

    public QryIopWindow(int maxDistance) {
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

            // find occurrences of the query args
            List<Integer> positions = new ArrayList<>();
            while (true) {
                // find the min and the max position among query arg
                int minPos = Integer.MAX_VALUE;
                int maxPos = Integer.MIN_VALUE;
                int minArgIdx = 0;
                int maxArgIdx = 0;

                boolean done = false;
                for (int i = 0; i < this.args.size(); ++i) {
                    QryIop q = (QryIop) this.args.get(i);
                    if (!q.locIteratorHasMatch()) {
                        done = true;
                        break;
                    }

                    int pos = q.locIteratorGetMatch();
                    if (pos < minPos) {
                        minPos = pos;
                        minArgIdx = i;
                    }
                    if (pos > maxPos) {
                        maxPos = pos;
                        maxArgIdx = i;
                    }
                }
                if (done) break;

                // Note: if this.args only has one arg, maxPos == minPos

                // check distance between the min position and max position
                // add to inverted list if valid
                if (maxPos >= minPos && maxPos - minPos < this.maxDistance) {
                    positions.add(((QryIop) this.args.get(maxArgIdx)).locIteratorGetMatch());

                    // consume all indices
                    for (Qry arg : this.args) {
                        ((QryIop) arg).locIteratorAdvance();
                    }
                } else {
                    // consume the min arg index
                    ((QryIop) this.args.get(minArgIdx)).locIteratorAdvance();
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
