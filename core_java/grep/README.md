# Introduction
This project is a mimic of the Linux `grep` command which allows users to search matching strings from files. The general idea is to the user gives a directory/file to look at and find all in the lines in file(s) of the given directory's based on a regex the user gives. The resulting lines put into a user given output file. This tool has been built using Core Java features, Lambda and Stream APIs, SLF4J for logging, and JUnit for testing. Development has been done using Maven and IntelliJ IDE with additional deployment being added by the usage of Docker for containerisation.

# Quick Start
## Building the App
0. Clone the project and go into the project directory using ```cd core_java/grep```

### Jar File:
1. Choose which grep app type you want to build (one stores all data in a list and the other uses Lambda and Stream APIs to reduce heap memory problems) in the ``pom.xml`` file on the following line:
```java
<mainClass>ca.jrvs.apps.grep.{JavaGrepImp or JavaGrepLambdaImp}</mainClass>
```
2. Compile and build the project using Maven
```
mvn clean package
```
3. Run the build
```
java -jar target/grep-1.0-SNAPSHOT.jar <regex> <rootDir> <outputFile>

// use the following version if you want to directly control which grep app type you are using
java -cp target/grep-1.0-SNAPSHOT.jar ca.jrvs.apps.grep.{JavaGrepImp or JavaGrepLambdaImp} <regex> <rootDir> <outputFile>

```
### Docker:
1. Create a Docker container using the Dockerfile given
```
docker build -t grep:local .
```
2. Run the program in the container
```
docker run --rm \
  -v "$(pwd)"/data:/data \
  -v "$(pwd)"/out:/out \
  grep:local <regex> <rootDir> <outputFile>
```
# Implemenation
## Pseudocode
This is the main `process` method pseudocode:
```java
matchedLines = []
for file in listFilesRecursively(rootDir)
  for line in readLines(file)
      if containsPattern(line)
        matchedLines.add(line)
writeToFile(matchedLines)
```

## Performance Issue
The default implementation of the project reads the data at once which can lead to heap memory problems if the dataset is big enough. This can be remedied by using a BufferReader but the problem remains when the data is collected into one big list. Thus, the Lambda version of the app uses Stream APIs to handle data one-by-one rather than storing all the data in the heap.

# Test
There are tests for both the Java Grep file without and with lambda & stream APIs. The test cover at least one scenario for each of the implemented methods using the shakespeare.txt file in the data/txt folder. Output files are also made with a temporary folder that creates and deletes itself after every test.

# Deployment
The app can be run using Docker as shown prior which allows the app to be distributed much more easily. You just need to create a Docker container using the Dockerfile given and then run the project using that container. Note, the base image is eclipse-temurin:8-jdk-alpine for the Dockerfile. Also, can be run and distributed by building and then sharing the jar file.
# Improvement
1. Explicitly using UTF-8 instead of the platform default charset (e.g. new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), so behavior is predictable across machines.
2. Clean up error handling and API consistency as the interface doesn't declare `throws IOException` on readLines, the project currently has a mixture of checked exceptions (process, writeToFile) and unchecked wrappers (RuntimeException from readLines).
3. Testing can be more extensive as only one test is given per method.