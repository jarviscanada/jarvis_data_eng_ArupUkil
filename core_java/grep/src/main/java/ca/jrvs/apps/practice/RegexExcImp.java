package ca.jrvs.apps.practice;

public class RegexExcImp implements RegexExc {

  // some quick tests
  public static void main(String[] args) {
    RegexExcImp regex = new RegexExcImp();

    System.out.println("matchJpeg tests");
    String[] jpegTests = {
        "photo.jpg",
        "IMAGE.JPEG",
        "icon.png",
        "no_extension",
        "weird.jpG"
    };

    for (String name : jpegTests) {
      System.out.println(name + " -> " + regex.matchJpeg(name));
    }

    System.out.println("\nmatchIp tests");
    String[] ipTests = {
        "0.0.0.0",
        "192.168.1.1",
        "999.999.999.999",
        "1.2.3",
        "1.2.3.4.5",
        "abc.def.ghi.jkl",
        "256.1.1.1"
    };
    for (String ip : ipTests) {
      System.out.println(ip + " -> " + regex.matchIp(ip));
    }

    System.out.println("\nisEmptyLine tests");
    String[] lineTests = {
        "",
        "   ",
        "\t\t",
        " \t \n",
        "not empty",
        "  x  "
    };
    for (String line : lineTests) {
      // show invisible whitespace by wrapping in []
      System.out.println("[" + line + "] -> " + regex.isEmptyLine(line));
    }
  }

  @Override
  public boolean matchJpeg(String filename) {
    if (filename == null) {
      return false;
    }
    // (?i) = case-insensitive
    return filename.matches("(?i).+\\.(jpg|jpeg)");
  }

  @Override
  public boolean matchIp(String ip) {
    if (ip == null) {
      return false;
    }
    return ip.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}");
  }

  @Override
  public boolean isEmptyLine(String line) {
    if (line == null) {
      return false;
    }
    return line.matches("\\s*");
  }
}
