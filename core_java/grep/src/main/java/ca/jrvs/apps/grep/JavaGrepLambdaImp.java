package ca.jrvs.apps.grep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaGrepLambdaImp extends JavaGrepImp {
//  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static void main(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("USAGE: JavaGrep <regex> <rootPath> <outFile>");
    }

    // Default logger config
    BasicConfigurator.configure();

    JavaGrepLambdaImp javaGrepLambdaImp = new JavaGrepLambdaImp();
    javaGrepLambdaImp.setRegex(args[0]);
    javaGrepLambdaImp.setRootPath(args[1]);
    javaGrepLambdaImp.setOutFile(args[2]);

    try {
      javaGrepLambdaImp.process();
    }
    catch (Exception e) {
      javaGrepLambdaImp.logger.error("Error: Unable to process", e);
    }
  }

  /**
   * Implemented using lambda and stream APIs
   */
  @Override
  public List<String> readLines(File inputFile) {
    if (inputFile == null) {
      throw new IllegalArgumentException("inputFile must not be null");
    }

    if (!inputFile.isFile()) {
      throw new IllegalArgumentException("Not a file: " + inputFile.getAbsolutePath());
    }

    try {
      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      Stream<String> lineStream = reader.lines();
      return lineStream.collect(Collectors.toList());
    }
    catch (IOException e) {
      logger.error("Failed to read file: {}", inputFile.getAbsolutePath(), e);
      throw new RuntimeException("Failed to read file " + inputFile.getAbsolutePath(), e);
    }
  }

  /**
   * Helper recursive function used for transversing a directory in a DFS style
   * Adds files found in the directory while transversing
   * @param file current file
   * @return Stream<File> stream of the files in the directory
   */
  private Stream<File> fileSearchHelper(File file) {
    if (file.isFile()) {
      return Stream.of(file);
    }

    File[] children = file.listFiles();

    if (children == null || children.length == 0) {
      // not a directory, or IO error / no permission / empty dir
      return Stream.empty();
    }

    // for each child, recursively get its files, and flatten them
    return Arrays.stream(children).flatMap(child -> fileSearchHelper(child));
  }

  /**
   * Implemented using lambda and stream APIs
   */
  @Override
  public List<File> listFiles(String rootDir) {
    File root = new File(rootDir);

    if (!root.exists()) {
      logger.warn("Root path does not exist: {}", rootDir);
      return Collections.emptyList();
    }

    return fileSearchHelper(root).collect(Collectors.toList());
  }
}
