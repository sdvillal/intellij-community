// "Replace Stream API chain with loop" "true"

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Test {
  static void test(List<String> list) {
    Map<Integer, Integer> map3 = list.stream().coll<caret>ect(Collectors.groupingBy(String::length, Collectors.mapping(String::trim, Collectors.summingInt(String::length))));
  }
}