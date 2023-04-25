package search_engine;

import java.util.stream.IntStream;
import java.util.stream.Stream;


public class HW1Tests extends QryEvalTests {
    private static Stream<String> fileNameSource() {
        return IntStream.range(0, 26).mapToObj(i -> "HW1-Train-" + i);
    }
}
