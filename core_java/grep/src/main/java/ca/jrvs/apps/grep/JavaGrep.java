package ca.jrvs.apps.grep;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface JavaGrep {

  /**
   * Top level search workflow
   * @throws IOException
   */
  void process() throws IOException;

  /**
   * Traverse a given directory and return all files
   * @param rootDir input directory
   * @return files under the rootDir
   */
  List<File> listFiles(String rootDir);

  /**
   * Read a file and return all the lines.
   * This is implemented via FileReader which a class that reads characters from a file.
   * It wraps a byte stream and automatically decodes bytes into characters using the
   * system's default character encoding (UTF-8 on many systems, but not guaranteed).
   * BufferedReader adds buffering on top of a Reader to reduce I/O calls and provides
   * methods like readLine() for convenient line-by-line reading.
   *
   * @param inputFile file to be read
   * @return lines
   * @throws IllegalArgumentException if given inputFile is not a file
   * @throws RuntimeException called when IOException when trying to read the file
   * The IOException isn't directly sent as it's a checked exception which the method
   * signature hasn't added. This leads to an asymmetry with the write method but this
   * was how the Notion page gave the interface as.
   */
  List<String> readLines(File inputFile);

  /**
   * Check if a line contains the regex pattern (passed by user)
   * @param line input string
   * @return true if there is a match
   */
  boolean containsPattern(String line);

  /**
   * Write lines ot a file
   * For implementation I used FileWriter rather than FileOutputStream and
   * OutputStreamWriter for both maintaining similarity in implementation with
   * readLines and that the class is just a convenience wrapper that internally uses
   * FileOutputStream and OutputStreamWriter. FileOutputStream writes raw bytes to a
   * file on disk and OutputStreamWriter does the character encoding by converting Java
   * chars into bytes using a charset (e.g. UTF-8). BufferedWriter is used to add a
   * buffer on top of a Writer (like OutputStreamWriter) to reduce the number of OS
   * writes and give nice methods like newLine. So in practice I'm actually doing
   * FileOutputStream -> OutputStreamWriter -> BufferedWriter.
   *
   * @param lines matched line
   * @throws IOException if write failed
   */
  void writeToFile(List<String> lines) throws IOException;

  String getRootPath();

  void setRootPath(String rootPath);

  String getRegex();

  void setRegex(String regex);

  String getOutFile();

  void setOutFile(String outFile);
}
