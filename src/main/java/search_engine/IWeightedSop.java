package search_engine;

public interface IWeightedSop {
    void appendWeightedArg(Qry q, double weight) throws IllegalArgumentException;
}
