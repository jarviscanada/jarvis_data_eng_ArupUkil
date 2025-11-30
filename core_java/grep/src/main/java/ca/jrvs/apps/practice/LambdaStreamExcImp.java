package ca.jrvs.apps.practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LambdaStreamExcImp implements LambdaStreamExc {

  // For testing purposes
  public static void main(String[] args) {
    LambdaStreamExc lse = new LambdaStreamExcImp();

    // toUpperCase
    System.out.println("toUpperCase example:");
    lse.toUpperCase("hello", "world").forEach(System.out::println);

    // filter
    System.out.println("\nfilter example (remove strings containing 'a'):");
    Stream<String> stringStream = lse.createStrStream("apple", "bob", "cat", "dog");
    lse.filter(stringStream, "a").forEach(System.out::println);
    // expected: only "bob" and "dog"

    // createIntStream(int[] arr) and toList(IntStream)
    System.out.println("\ncreateIntStream(int[] arr) + toList example:");
    int[] arr = {1, 2, 3, 4, 5};
    List<Integer> intList = lse.toList(lse.createIntStream(arr));
    System.out.println(intList);

    // createIntStream(start, end)
    System.out.println("\ncreateIntStream(0, 5) example:");
    lse.createIntStream(0, 5).forEach(System.out::println);

    // squareRootIntStream
    System.out.println("\nsquareRootIntStream(1..5) example:");
    lse.squareRootIntStream(lse.createIntStream(1, 5)).forEach(System.out::println);

    // getOdd
    System.out.println("\ngetOdd(0..10) example:");
    lse.getOdd(lse.createIntStream(0, 10)).forEach(System.out::println);

    // getLambdaPrinter example from Javadoc
    System.out.println("\ngetLambdaPrinter example:");
    Consumer<String> printer = lse.getLambdaPrinter("start>", "<end");
    printer.accept("Message body");
    // expected: start>Message body<end

    // printMessages example from Javadoc
    System.out.println("\nprintMessages example:");
    String[] messages = {"a", "b", "c"};
    lse.printMessages(messages, lse.getLambdaPrinter("msg:", "!"));
    // expected:
    // msg:a!
    // msg:b!
    // msg:c!

    // printOdd example from Javadoc
    System.out.println("\nprintOdd example:");
    lse.printOdd(lse.createIntStream(0, 5), lse.getLambdaPrinter("odd number:", "!"));
    // expected:
    // odd number:1!
    // odd number:3!
    // odd number:5!

    // flatNestedInt example (square and flatten)
    System.out.println("\nflatNestedInt example:");
    List<Integer> list1 = new ArrayList<>();
    list1.add(1);
    list1.add(2);
    list1.add(3);

    List<Integer> list2 = new ArrayList<>();
    list2.add(4);
    list2.add(5);

    Stream<List<Integer>> nested = Stream.of(list1, list2);
    lse.flatNestedInt(nested).forEach(System.out::println);
    // iexpected: 1, 4, 9, 16, 25
  }

  @Override
  public Stream<String> createStrStream(String... strings) {
    return Stream.of(strings);
  }

  @Override
  public Stream<String> toUpperCase(String... strings) {
    return createStrStream(strings).map(String::toUpperCase);
  }

  @Override
  public Stream<String> filter(Stream<String> stringStream, String pattern) {
    return stringStream.filter(s -> !s.contains(pattern));
  }

  @Override
  public IntStream createIntStream(int[] arr) {
    return IntStream.of(arr);
  }

  @Override
  public <E> List<E> toList(Stream<E> stream) {
    return stream.collect(Collectors.toList());
  }

  @Override
  public List<Integer> toList(IntStream intStream) {
    // boxed(): IntStream -> Stream<Integer>
    return intStream.boxed().collect(Collectors.toList());
  }

  @Override
  public IntStream createIntStream(int start, int end) {
    return IntStream.rangeClosed(start, end);
  }

  @Override
  public DoubleStream squareRootIntStream(IntStream intStream) {
    return intStream.mapToDouble(Math::sqrt);
  }

  @Override
  public IntStream getOdd(IntStream intStream) {
    return intStream.filter(i -> i % 2 != 0);
  }

  @Override
  public Consumer<String> getLambdaPrinter(String prefix, String suffix) {
    return msg -> System.out.println(prefix + msg + suffix);
  }

  @Override
  public void printMessages(String[] messages, Consumer<String> printer) {
    createStrStream(messages).forEach(printer);
  }

  @Override
  public void printOdd(IntStream intStream, Consumer<String> printer) {
    intStream.filter(i -> i % 2 != 0).forEach(i -> printer.accept(String.valueOf(i)));
  }

  @Override
  public Stream<Integer> flatNestedInt(Stream<List<Integer>> ints) {
    return ints.flatMap(list -> list.stream().map(n -> n * n));
  }
}
