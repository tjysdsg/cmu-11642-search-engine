package search_engine;

import java.util.stream.IntStream;
import java.util.stream.Stream;


public class HW2Tests extends QryEvalTests {
    private static Stream<String> fileNameSource() {
        return IntStream.range(0, 51).mapToObj(i -> "HW2-Train-" + i);
    }
}
