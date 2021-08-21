import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
import tasks.Task;

public class DukeStorage {

  public static final ArrayList<Task> tasksList = new ArrayList<>();

  public static void readFromDatabase() {
    String dukeLocation = System.getProperty("user.dir");
    Path filePath = Paths.get(dukeLocation, "data", "tasks");

    if (!Files.isRegularFile(filePath)) {
      return;
    }

    Duke.renderOutput("Reading last save  - " + filePath);
    ArrayList<String> failedParses = new ArrayList<>();

    try {
      Stream<String> databaseToString = Files.lines(filePath);
      tasksList.clear();
      databaseToString.forEach(
        taskString -> {
          try {
            Task task = Task.stringToTask(taskString);
            tasksList.add(task);
          } catch (IllegalArgumentException e) {
            failedParses.add(taskString);
          }
        }
      );
      databaseToString.close();
    } catch (IOException e) {
      Duke.renderOutput("Save file could not be read. It will be wiped on the next save.");
    }

    if (failedParses.size() > 0) {
      String status = String.format(
        "Save file was read partially. %d tasks added, %d tasks could not be understood:\n",
        tasksList.size(),
        failedParses.size()
      );
      status +=
        "Failed to parse\n:" + failedParses.stream().map(x -> x + "\n").reduce("", (a, b) -> a + b);
      Duke.renderOutput(status);
    }
  }

  public static void writeToDatabase() {
    String dukeLocation = System.getProperty("user.dir");
    Path folderPath = Paths.get(dukeLocation, "data");
    Path filePath = Paths.get(dukeLocation, "data", "tasks");

    StringBuilder save = new StringBuilder();

    tasksList.forEach(x -> save.append(x.taskToString() + System.lineSeparator()));
    byte[] saveResult = save.toString().getBytes();

    Duke.renderOutput("Saving tasks  - " + filePath);
    Duke.renderOutput("DEBUG: Text to be saved \n" + save);

    try {
      if (!Files.isDirectory(folderPath)) {
        Files.createDirectory(folderPath);
      }
      if (Files.isRegularFile(filePath)) {
        Files.delete(filePath);
      }
      Files.write(filePath, saveResult);
    } catch (IOException e) {
      // TODO: Handle this properly in the future
      Duke.renderOutput("File could not be saved. - " + e.getMessage());
    }
  }
}
