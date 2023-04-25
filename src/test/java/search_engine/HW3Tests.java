package search_engine;

import java.util.stream.IntStream;
import java.util.stream.Stream;


public class HW3Tests extends QryEvalTests {
    private static Stream<String> fileNameSource() {
        return IntStream.range(0, 26).mapToObj(i -> "HW3-Train-" + i);
    }
}
