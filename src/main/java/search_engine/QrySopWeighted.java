package search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class QrySopWeighted extends QrySop implements IWeightedSop {
    protected final List<Double> weights = new ArrayList<>();
    protected double weightSum = 0;

    @Override
    public void initialize(RetrievalModel r) throws IOException {
        super.initialize(r);

        assert this.weights.size() > 0;
        assert this.weights.size() == this.args.size();
        for (double w : this.weights) {
            weightSum += w;
        }
    }

    @Override
    public void appendWeightedArg(Qry q, double weight) throws IllegalArgumentException {
        this.appendArg(q);
        this.weights.add(weight);

        assert this.weights.size() == this.args.size();
    }

    @Override
    public String toString() {
        String result = new String();

        for (int i = 0; i < this.args.size(); i++)
            result += this.weights.get(i) + " " + this.args.get(i) + " ";

        return (this.getDisplayName() + "( " + result + ")");
    }
}
