package duke;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import duke.tasks.InvalidTaskException;
import duke.tasks.Task;
import duke.tasks.TaskList;
import javafx.application.Platform;


/**
 * Handles matching input to behaviour and execution action.
 * Using function `takeInput` with the input string will run the function
 * and return true or false (whether or not to continue monitoring input)
 */
public class Parser {

    private final TaskList tasks;

    protected Parser(TaskList taskList) {
        this.tasks = taskList;
    }

    enum Actions {
        DELETE,
        MARK_COMPLETE,
    }

    /**
     * Takes in and handles input for Duke (the logic).
     *
     * @param input User input passed in to Duke.
     * @return String result to be output
     */
    public String takeInput(String input) {
        if (matches("bye", input)) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    Platform.exit();
                    System.exit(0);
                }
            }, 1600);
            return "Goodbye!";
        } else if (matches("", input)) {
            return "";
        } else if (matches("list", input)) {
            return listTasks();
        } else if (startsWithOrEquals("find ", input)) {
            return findTasks(getArgs(input, "find "));
        } else if (startsWithOrEquals("done ", input)) {
            return doTaskAction(getArgs(input, "done "), Actions.MARK_COMPLETE);
        } else if (startsWithOrEquals("delete ", input)) {
            return doTaskAction(getArgs(input, "delete "), Actions.DELETE);
        } else if (startsWithOrEquals("todo ", input)) {
            return addTask(getArgs(input, "todo "), Task.Type.TODO);
        } else if (startsWithOrEquals("deadline ", input)) {
            return addTask(getArgs(input, "deadline "), Task.Type.DEADLINE);
        } else if (startsWithOrEquals("event ", input)) {
            return addTask(getArgs(input, "event "), Task.Type.EVENT);
        } else if (matches("reset", input)) {
            tasks.clear();
            assert tasks.size() == 0 : "Tasks were not cleared";
            return "Cleared";
        } else {
            return "I did not understand, sorry!";
        }
    }

    private boolean matches(String phrase, String input) {
        return input.trim().equalsIgnoreCase(phrase);
    }

    private boolean startsWithOrEquals(String phrase, String input) {
        return input.trim().startsWith(phrase) || input.trim().equalsIgnoreCase(phrase.trim());
    }

    private boolean contains(String phrase, String input) {
        return input.trim().contains(phrase);
    }

    private String getArgs(String input, String command) {
        return input.substring(input.toLowerCase().indexOf(command) + command.length()).trim();
    }

    private String listTasks() {
        int taskCount = 1;
        StringBuilder result = new StringBuilder();
        List<Task> taskList = this.tasks.stream().sorted(Task::chronologicalComparator).collect(Collectors.toList());
        for (Task task : taskList) {
            result.append(String.format("%2d. %s\n", taskCount++, task));
        }
        return result.toString();
    }

    private String findTasks(String searchQuery) {
        int taskCount = 1;
        StringBuilder result = new StringBuilder();
        Stream<Task> filteredTasks = this.tasks.stream().filter(task -> task.toString().contains(searchQuery));
        for (Task task : filteredTasks.collect(Collectors.toList())) {
            result.append(String.format("%2d. %s\n", taskCount++, task));
        }
        return result.toString();
    }

    private String addTask(String taskName, Task.Type type) {
        try {
            Task task = Task.createTask(taskName.trim(), type);
            tasks.add(task);
            return "added: " + task;
        } catch (InvalidTaskException err) {
            return err.getMessage();
        }
    }

    private String doTaskAction(String taskNumStr, Actions action) {
        try {
            int taskNum = Integer.parseInt(taskNumStr);
            Task task = tasks.get(taskNum - 1);
            String output = "";
            switch (action) {
            case DELETE:
                tasks.remove(taskNum - 1);
                output += "Noted. I have deleted the following:\n    " + task;
                output += String.format(
                    "\nYou now have %d tasks in the list", tasks.size());
                break;
            case MARK_COMPLETE:
                task.setComplete(true);
                output += "Great! I've marked this task as done:\n    " + task;
                break;
            default:
                assert false : "Action not in enum Action";
            }
            return output;
        } catch (NumberFormatException err) {
            return "Which task are you interacting with?\n"
                + "USAGE:\n{action} {task number}\n"
                + "Example: > done 4\n"
                + "         > delete 2\n"
                + "Try the `list` command to see the list of tasks";
        } catch (IndexOutOfBoundsException err) {
            return "There is no task at that index.";
        }
    }
}
