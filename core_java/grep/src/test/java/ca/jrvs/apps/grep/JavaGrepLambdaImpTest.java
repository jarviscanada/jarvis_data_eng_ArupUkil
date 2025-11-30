package ca.jrvs.apps.grep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaGrepLambdaImpTest {
  private JavaGrepLambdaImp grep;

  @BeforeEach
  void setup() {
    grep = new JavaGrepLambdaImp();
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
  public void testListFiles() {
    List<File> files = grep.listFiles(grep.getRootPath());

    assertFalse(files.isEmpty());
    assertTrue(files.stream().anyMatch(f -> f.getName().equals("shakespeare.txt")));
  }
}
