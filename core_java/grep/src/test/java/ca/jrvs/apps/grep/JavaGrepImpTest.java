package ca.jrvs.apps.grep;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class JavaGrepImpTest {
  @TempDir
  Path tempDir;

  private JavaGrepImp grep;

  @BeforeAll
  static void setupLogging() {
    BasicConfigurator.configure();
  }

  @BeforeEach
  void setup() {
    grep = new JavaGrepImp();
    grep.setRootPath("data/txt");
    grep.setRegex(".*Romeo.*Juliet.*");
  }

  @Test
  public void testReadLines() {
    File input = new File("data/txt/shakespeare.txt");
    List<String> lines = grep.readLines(input);

    assertFalse(lines.isEmpty());

    // Checks if the first line matches
    String first = lines.get(0).trim();
    assertEquals(
        "This is the 100th Etext file presented by Project Gutenberg, and",
        first
    );
  }

  @Test
  public void testReadLinesDirectoryThrows() {
    // Passing a directory instead of a file should throw
    File dir = new File(grep.getRootPath());
    assertThrows(IllegalArgumentException.class, () -> grep.readLines(dir));
  }

  @Test
  public void testContainsPattern() {
    assertFalse(grep.containsPattern("Shakespeare"));
    assertTrue(grep.containsPattern("Romeo and Juliet"));
    assertFalse(grep.containsPattern("romeo and juliet"));
  }

  @Test
  public void testListFiles() {
    List<File> files = grep.listFiles(grep.getRootPath());

    assertFalse(files.isEmpty());
    assertTrue(files.stream().anyMatch(f -> f.getName().equals("shakespeare.txt")));
  }

  @Test
  public void testWriteToFile() throws IOException {
    Path out = tempDir.resolve("out.txt");
    grep.setOutFile(out.toString());
    List<String> sample = Arrays.asList("line1", "line2", "line3");

    grep.writeToFile(sample);

    List<String> written = Files.readAllLines(out);
    assertEquals(sample, written);
  }

  @Test
  public void testProcess() throws IOException {
    Path out = tempDir.resolve("out.txt");
    grep.setOutFile(out.toString());

    grep.process();

    List<String> result = Files.readAllLines(out);
    assertFalse(result.isEmpty());
    assertEquals(4, result.size());

    for (String line : result) {
      assertTrue(line.contains("Romeo") && line.contains("Juliet"));
    }
  }
}
