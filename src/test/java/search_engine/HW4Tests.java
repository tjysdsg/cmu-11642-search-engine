package search_engine;

import java.util.stream.IntStream;
import java.util.stream.Stream;


public class HW4Tests extends QryEvalLTRTests {
    private static Stream<String> fileNameSource() {
        return IntStream.range(0, 26).mapToObj(i -> "HW4-Train-" + i);
    }
}
