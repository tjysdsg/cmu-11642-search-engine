package search_engine;

import java.util.stream.IntStream;
import java.util.stream.Stream;


public class HW5Tests extends QryEvalTests {
    private static Stream<String> fileNameSource() {
        return IntStream.range(1, 25).mapToObj(i -> "HW5-Train-" + i);
    }
}
