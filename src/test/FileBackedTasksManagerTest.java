package test;

import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import service.FileBackedTasksManager;
import service.ManagerSaveException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private final String path = "src" + File.separator + "resources" + File.separator + "log.csv";
    private final File file = new File(path);

    @Override
    void setTaskManager() {
        taskManager = new FileBackedTasksManager(path);
    }

    @Test
    void save() {
        //File fileEmptyPath = new File("");
        FileBackedTasksManager taskManagerEmptyPath = new FileBackedTasksManager("");
        ManagerSaveException ex = assertThrows(
                ManagerSaveException.class,
                taskManagerEmptyPath::save
        );
        assertEquals("Ошибка при сохранениив в файл", ex.getMessage());

        taskManager.addTask(task);
        FileBackedTasksManager taskManagerFromFile = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(taskManagerFromFile, "Файл не записан");
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");

        taskManager.addEpic(epic);
        taskManagerFromFile = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(taskManagerFromFile, "Файл не записан");
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");

        subtask.setStartTime(Optional.of(TEST_TIME));
        subtask.setDuration(Optional.of(1L));
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        taskManagerFromFile = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(taskManagerFromFile, "Файл не записан");
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");

        taskManager.getTask(task.getId());
        taskManagerFromFile = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(taskManagerFromFile, "Файл не записан");
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");

        taskManager.getEpic(epicId);
        taskManagerFromFile = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(taskManagerFromFile, "Файл не записан");
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");
    }

    @Test
    void testToString() {
        taskManager.addTask(task);
        final int taskId = task.getId();
        taskManager.addEpic(epic);
        final int epicId = epic.getId();
        subtask.setDuration(Optional.of(15L));
        subtask.setStartTime(Optional.of(TEST_TIME));
        taskManager.addSubtask(subtask, epicId);
        final int subtaskId = subtask.getId();

        //id,type,name,status,description,epic,duration,startTime
        String expectedTaskToString = taskId + ",TASK,Task,NEW,Task description, , , , ";
        String expectedEpicToString = epicId + ",EPIC,Epic,NEW,Epic description, ,"
                + "15," + TEST_TIME.format(Task.formatter) + "," + TEST_TIME.plusMinutes(15L).format(Task.formatter);
        String expectedSubtaskToString = subtaskId + ",SUBTASK,Subtask,NEW,Subtask description," + epicId
                + ",15," + TEST_TIME.format(Task.formatter) + ", ";
        //Task
        String taskToString = FileBackedTasksManager.toString(task);
        assertEquals(expectedTaskToString, taskToString, "Строки не совпадают");
        //Epic
        String epicToString = FileBackedTasksManager.toString(epic);
        assertEquals(expectedEpicToString, epicToString, "Строки не совпадают");
        //Subtask
        String subtaskToString = FileBackedTasksManager.toString(subtask);
        assertEquals(expectedSubtaskToString, subtaskToString, "Строки не совпадают");
    }

    @Test
    void fromString() {
        //пустая строка
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.fromString("")
        );
        assertEquals("Чтение пустой строки невозможно", ex.getMessage());

        //стандартный случай
        String taskFromString = "0,TASK,Task,NEW,Task description, , , , ";
        String subtaskFromString = "2,SUBTASK,Subtask,NEW,Subtask description,1,15,01.01.2023 10:30, ";

        Task taskExpected = task;
        Subtask subtaskExpected = subtask;
        subtaskExpected.setId(2);
        subtaskExpected.setEpicId(1);
        subtaskExpected.setDuration(Optional.of(15L));
        subtaskExpected.setStartTime(Optional.of(TEST_TIME));

        Task taskRead = taskManager.fromString(taskFromString);
        Subtask subtaskRead = (Subtask) taskManager.fromString(subtaskFromString);

        assertNotNull(taskRead);
        assertNotNull(subtaskRead);

        assertEquals(taskExpected, taskRead, "Задачи не совпадают");
        assertEquals(subtaskExpected, subtaskRead, "Подзадачи не совпадают");
    }

    @Test
    void historyToString() {
        taskManager.addTask(task);
        int taskId = task.getId();
        taskManager.addEpic(epic);
        int epicId = epic.getId();

        String historyLine = FileBackedTasksManager.historyToString(taskManager.getHistoryManager());
        assertEquals("", historyLine, "История не пустая");

        taskManager.getTask(taskId);
        historyLine = FileBackedTasksManager.historyToString(taskManager.getHistoryManager());
        assertEquals(1, historyLine.length(), "Количество задач в строке не совпадает");
        assertEquals(String.valueOf(taskId), historyLine, "Задачи не совпадают");

        taskManager.getEpic(epicId);
        historyLine = FileBackedTasksManager.historyToString(taskManager.getHistoryManager());
        assertEquals(2, historyLine.length() / 2 + 1, "Количество задач в строке не совпадает");
        assertEquals(taskId + "," + epicId, historyLine, "Задачи не совпадают");
    }

    @Test
    void historyFromString() {
        String historyLine = "";
        List<Integer> history = FileBackedTasksManager.historyFromString(historyLine);
        assertEquals(Collections.emptyList(), history, "История не пустая");

        historyLine = "1";
        history = FileBackedTasksManager.historyFromString(historyLine);
        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Неверное количество задач в истории");
        assertEquals(List.of(1), history, "Задачи в истории не совпадают");

        historyLine = "1,2";
        history = FileBackedTasksManager.historyFromString(historyLine);
        assertNotNull(history, "История не возвращается");
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(List.of(1, 2), history, "Задачи в истории не совпадают");
    }

    @Test
    void loadFromFile() {
        //все тесты пересекаются с save()
        //пустой файл
        FileBackedTasksManager taskManagerFromFile = new FileBackedTasksManager(path);
        assertNotNull(taskManagerFromFile);
        assertEquals(taskManager, taskManagerFromFile, "Менеджеры не совпадают");
    }
}