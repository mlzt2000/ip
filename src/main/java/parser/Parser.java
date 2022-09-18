package parser;

import exception.DukeException;
import task.Deadline;
import task.Event;
import task.Task;
import task.ToDo;
import tasklist.TaskList;
import ui.Ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents the object that converts user input into commands and tasks.
 */
public class Parser {
    private static final String END_COMMAND = "bye";
    private static final String PRINT_COMMAND = "list";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String DELETE_COMMAND = "delete";
    private static final String FIND_COMMAND = "find";

    private static final String TODO_SEPARATOR = "/";
    private static final String DEADLINE_SEPARATOR = "/by";
    private static final String EVENT_SEPARATOR = "/at";

    private final Ui ui;
    private final TaskList taskList;

    private boolean isEnded = false;

    public Parser(Ui ui, TaskList taskList) {
        this.ui = ui;
        this.taskList = taskList;
    }

    public boolean hasNext() {
        return !isEnded;
    }

    /**
     * Recognises the command given by the user and calls the relevant handler to manage user input.
     */
    public String handleNext(String input) {
        try {
            String inputCmd = getNext(input);
            String inputRem = getNextLine(input).strip();
            switch (inputCmd) {
            case (END_COMMAND):
                return closeParser();
            case (PRINT_COMMAND):
                return printAllTasks();
            case (MARK_COMMAND):
                return markTask(inputRem);
            case (UNMARK_COMMAND):
                return unmarkTask(inputRem);
            case (TODO_COMMAND):
                return createAndAddTodo(inputRem);
            case (DEADLINE_COMMAND):
                return createAndAddDeadline(inputRem);
            case (EVENT_COMMAND):
                return createAndAddEvent(inputRem);
            case (DELETE_COMMAND):
                return deleteTask(inputRem);
            case (FIND_COMMAND):
                return findTasks(inputRem);
            default:
                throw new DukeException("Unexpected task type!");
            }
        } catch (DukeException de) {
            return ui.printException(de);
        }
    }

    private String closeParser() {
        this.isEnded = true;
        return this.ui.printGoodbye();
    }

    private String printAllTasks() {
        return this.ui.printAll(this.taskList);
    }

    private String markTask(String indexStr) throws DukeException {
        try {
            int index = Integer.parseInt(indexStr);
            Task task = taskList.markTask(--index);
            return ui.printTaskMarked(task);
        } catch (NumberFormatException e) {
            throw DukeException.markTaskException("Please input a valid number!");
        } catch (DukeException de) {
            throw DukeException.markTaskException(de.toString());
        }
    }

    private String unmarkTask(String indexStr) throws DukeException {
        try {
            int index = Integer.parseInt(indexStr);
            Task task = taskList.unmarkTask(--index);
            return ui.printTaskUnmarked(task);
        } catch (NumberFormatException e){
            throw DukeException.unmarkTaskException("Please input a valid number!");
        } catch (DukeException de) {
            throw DukeException.unmarkTaskException(de.toString());
        }
    }

    private String createAndAddTodo(String str) throws DukeException {
        try {
            String description = getValidDescription(str, TODO_SEPARATOR);
            Task newTodo = new ToDo(description);
            return addTask(newTodo);
        } catch (DukeException de) {
            throw DukeException.createTaskException(de.toString());
        }
    }

    private String createAndAddDeadline(String str) throws DukeException {
        try {
            String description = getValidDescription(str, DEADLINE_SEPARATOR);
            LocalDateTime by = getValidDateTime(str, DEADLINE_SEPARATOR);
            Task newDeadline = new Deadline(description, by);
            return addTask(newDeadline);
        } catch (DukeException de) {
            throw DukeException.createTaskException(de.toString());
        }
    }

    private String createAndAddEvent(String str) throws DukeException {
        try {
            String description = getValidDescription(str, EVENT_SEPARATOR);
            LocalDateTime at = getValidDateTime(str, EVENT_SEPARATOR);
            Task newEvent = new Event(description, at);
            return addTask(newEvent);
        } catch (DukeException de) {
            throw DukeException.createTaskException(de.toString());
        }
    }

    private String getValidDescription(String str, String separator) throws DukeException {
        String description = getFirstToken(str, separator);
        if (description.equals("")) {
            throw DukeException.taskDescriptionException("No description provided");
        }
        return description;
    }

    private LocalDateTime getValidDateTime(String str, String separator) throws DukeException {
        String datetimeStr = getSecondToken(str, separator);
        if (datetimeStr.equals("")) {
            throw DukeException.taskDateTimeException("No date time provided");
        }
        try {
            return LocalDateTime.parse(
                    datetimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm"));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw DukeException.taskDateTimeException("Date time should be in the format yyyy-MM-dd HHmm!");
        }
    }

    private String getFirstToken(String str, String separator) throws DukeException {
        String[] tokens = tokenize(str, separator);
        return tokens[0].strip();
    }

    private String getSecondToken(String str, String separator) throws DukeException {
        String[] tokens = tokenize(str, separator);
        if (tokens.length < 2) {
            throw DukeException.dateTimeTokenException();
        }
        return tokens[1].strip();
    }

    private String[] tokenize(String str, String separator) throws DukeException {
        str = str.strip();
        if (str.equals("")) {
            throw DukeException.taskDescriptionException("No input");
        }
        String[] tokens = str.split(separator);
        if (tokens.length < 1 || tokens.length > 2) {
            throw DukeException.taskTokenException(tokens.length);
        }
        return tokens;
    }

    private String addTask(Task task) throws DukeException {
        taskList.addTask(task);
        return ui.printTaskCreated(task);
    }

    private String deleteTask(String indexStr) throws DukeException {
        try {
            int index = Integer.parseInt(indexStr);
            Task task = taskList.deleteTask(--index);
            return ui.printTaskDeleted(task);
        } catch (NumberFormatException e) {
            throw DukeException.deleteTaskException("Please input a valid number!");
        } catch (DukeException de) {
            throw DukeException.deleteTaskException(de.toString());
        }
    }

    private String[] splitInput(String input) {
        return input.split(" ", 2);
    }

    private String getNext(String input) {
        return splitInput(input)[0];
    }

    private String getNextLine(String input) {
        String[] splitInput = splitInput(input);
        if (splitInput.length < 2) {
            return "";
        }
        return splitInput[1];
    }

    private String findTasks(String keyword) throws DukeException {
        try {
            String validKeyword = getValidKeyword(keyword);
            Task[] foundTasks = taskList.findTasks(validKeyword);
            return ui.printFoundTasks(foundTasks);
        } catch (DukeException de) {
            throw DukeException.findTaskException(de.toString());
        }
    }

    private String getValidKeyword(String keyword) throws DukeException {
        keyword = keyword.strip();
        if (keyword.equals("")) {
            throw DukeException.keywordException("No keyword provided");
        }
        String[] keywords = keyword.split(" ");
        if (keywords.length > 1) {
            throw DukeException.keywordException("Only one keyword expected");
        }
        return keyword;
    }
}
