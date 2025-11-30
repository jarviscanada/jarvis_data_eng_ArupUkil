package ca.jrvs.apps.grep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaGrepImp implements JavaGrep {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private String regex;
  private String rootPath;
  private String outFile;

  public static void main(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("USAGE: JavaGrep <regex> <rootPath> <outFile>");
    }

    // Default logger config
    BasicConfigurator.configure();

    JavaGrepImp javaGrepImp = new JavaGrepImp();
    javaGrepImp.setRegex(args[0]);
    javaGrepImp.setRootPath(args[1]);
    javaGrepImp.setOutFile(args[2]);

    try {
      javaGrepImp.process();
    }
    catch (Exception e) {
      javaGrepImp.logger.error("Error: Unable to process", e);
    }
  }

  @Override
  public void process() throws IOException {
    // Defensive check
    if (rootPath == null || regex == null || outFile == null) {
      throw new IllegalStateException("regex, rootPath, and outFile must be set before process()");
    }

    List<String> matchedLines = new ArrayList<>();

    List<File> files = listFiles(rootPath);
    logger.info("Found {} files under {}", files.size(), rootPath);

    for (File file : files) {
      for (String line : readLines(file)) {
        if (containsPattern(line)) {
          matchedLines.add(line);
        }
      }
    }

    writeToFile(matchedLines);
    logger.info("Wrote {} matched lines to {}", matchedLines.size(), outFile);
  }

  /**
   * Helper recursive function used for transversing a directory in a DFS style
   * Adds files found in the directory while transversing
   * @param file current file
   * @param files collection of files found thus far
   */
  private void fileSearchHelper(File file, List<File> files) {
    if (file.isFile()) {
      files.add(file);
      return;
    }

    File[] children = file.listFiles();

    // Check to handle case when the file is an empty directory
    if (children != null) {
      for (File child : children) {
        fileSearchHelper(child, files);
      }
    }
  }

  @Override
  public List<File> listFiles(String rootDir) {
    List<File> res = new ArrayList<>();
    File root = new File(rootDir);

    if (!root.exists()) {
      logger.warn("Root path does not exist: {}", rootDir);
      return res;
    }

    fileSearchHelper(root, res);

    return res;
  }

  @Override
  public List<String> readLines(File inputFile) {
    if (inputFile == null) {
      throw new IllegalArgumentException("inputFile must not be null");
    }

    if (!inputFile.isFile()) {
      throw new IllegalArgumentException("Not a file: " + inputFile.getAbsolutePath());
    }

    List<String> lines = new ArrayList<>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      String line = reader.readLine();
      while (line != null) {
        lines.add(line);
        line = reader.readLine();
      }
    }
    catch (IOException e) {
      logger.error("Failed to read file: {}", inputFile.getAbsolutePath(), e);
      throw new RuntimeException("Failed to read file " + inputFile.getAbsolutePath(), e);
    }

    return lines;
  }

  @Override
  public boolean containsPattern(String line) {
    return Pattern.compile(regex).matcher(line).find();
  }

  @Override
  public void writeToFile(List<String> lines) throws IOException {
    File out = new File(outFile);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
  }

  @Override
  public String getRootPath() {
    return rootPath;
  }

  @Override
  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  @Override
  public String getRegex() {
    return regex;
  }

  @Override
  public void setRegex(String regex) {
    this.regex = regex;
  }

  @Override
  public String getOutFile() {
    return outFile;
  }

  @Override
  public void setOutFile(String outFile) {
    this.outFile = outFile;
  }
}
