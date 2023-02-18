package test;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    //Со стандартным поведением.
    //С пустым списком задач.
    //С неверным id (пустой и/или несуществующий).
    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;
    protected static final LocalDateTime TEST_TIME =
            LocalDateTime.of(2023, 1, 1, 10, 30);

    void setTaskManager() {
    }

    @BeforeEach
    void setUp() {
        setTaskManager();
        task = new Task("Task", "Task description");
        epic = new Epic("Epic", "Epic description");
        subtask = new Subtask("Subtask", "Subtask description");
    }

    @Test
    void getTasksList() {
        List<Task> tasks = taskManager.getTasks();
        assertEquals(0, tasks.size(), "Список задач не пустой");
        //остальные случаи в addTask(), updateTask(), removeTask(), clearTasks()
    }

    @Test
    void getEpicsList() {
        List<Epic> epics = taskManager.getEpics();
        assertEquals(0, epics.size(), "Список эпиков не пустой");
        //остальные случаи в addEpic(), updateEpic(), removeEpic(), clearEpic()
    }

    @Test
    void getSubtasksList() {
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(0, subtasks.size(), "Список подзадач не пустой");
        //остальные случаи в addSubtask(), updateSubtask(), removeSubtask(), clearSubtasks()
    }

    @Test
    void getEpicSubtasks() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getEpicSubtasks(1)
        );
        assertEquals("Эпика с id 1 не существует", ex.getMessage());

        taskManager.addEpic(epic);
        int epicId = epic.getId();

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(0, epicSubtasks.size(), "Список подзадач не пустой");

        taskManager.addSubtask(subtask, epicId);
        int subtaskId = subtask.getId();
        epicSubtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(1, epicSubtasks.size(), "Количество подзадач не совпадает");
        assertEquals(subtask, epicSubtasks.get(0), "Задачи не совпадают");

        taskManager.removeSubtask(subtaskId);
        epicSubtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(0, epicSubtasks.size(), "Список подзадач не пустой");
    }

    @Test
    void getPrioritizedTasks() {
        //пустой список
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");

        //две задачи с незаданным временем
        taskManager.addTask(task);

        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);

        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(2, prioritizedTasks.size(), "Количество задач по приоритету не совпадает");
        assertEquals(List.of(task, subtask), prioritizedTasks, "Задачи по приоритету не совпадают");

        //+ задача с заданным временем
        Task task2 = new Task("Task2", "Task2 description");
        task2.setStartTime(Optional.of(TEST_TIME));
        task2.setDuration(Optional.of(1L));
        taskManager.addTask(task2);

        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(3, prioritizedTasks.size(), "Количество задач по приоритету не совпадает");
        assertEquals(List.of(task2, task, subtask), prioritizedTasks, "Задачи по приоритету не совпадают");

        //+ две задачи с заданным временем
        Task task3 = new Task("Task3", "Task3 description");
        task3.setStartTime(Optional.of(TEST_TIME.plusMinutes(60L)));
        task3.setDuration(Optional.of(1L));
        taskManager.addTask(task3);

        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(4, prioritizedTasks.size(), "Количество задач по приоритету не совпадает");
        assertEquals(List.of(task2, task3, task, subtask), prioritizedTasks, "Задачи по приоритету не совпадают");

        //остальные случаи в add, update, remove, clear
    }

    @Test
    void clearTasks() {
        taskManager.clearTasks();
        List<Task> tasks = taskManager.getTasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(tasks, "Список задач не возвращается");
        assertEquals(0, tasks.size(), "Список задач не пустой");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");

        taskManager.addTask(task);
        taskManager.getTask(task.getId());

        taskManager.clearTasks();
        tasks = taskManager.getTasks();
        prioritizedTasks = taskManager.getPrioritizedTasks();
        history = taskManager.getHistoryManager().getHistory();

        assertNotNull(tasks, "Список задач не возвращается");
        assertEquals(0, tasks.size(), "Список задач не пустой");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
    }

    @Test
    void clearEpics() {
        taskManager.clearEpics();
        List<Epic> epics = taskManager.getEpics();
        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(epics, "Список задач не возвращается");
        assertEquals(0, epics.size(), "Список задач не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");

        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        int subtaskId = subtask.getId();
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);

        taskManager.clearEpics();
        epics = taskManager.getEpics();
        history = taskManager.getHistoryManager().getHistory();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(epics, "Список задач не возвращается");
        assertEquals(0, epics.size(), "Список задач не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
    }

    @Test
    void clearSubtasks() {
        taskManager.clearSubtasks();
        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(subtasks, "Список подзадач не возвращается");
        assertEquals(0, subtasks.size(), "Список задач не пустой");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");

        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);

        taskManager.clearSubtasks();
        subtasks = taskManager.getSubtasks();
        prioritizedTasks = taskManager.getPrioritizedTasks();
        history = taskManager.getHistoryManager().getHistory();

        assertEquals(0, taskManager.getEpicSubtasks(epicId).size(), "Список подзадач в эпике не пустой");
        assertNotNull(subtasks, "Список подзадач не возвращается");
        assertEquals(0, subtasks.size(), "Список задач не пустой");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
    }

    @Test
    void getTask() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getTask(1)
        );
        assertEquals("Задачи с id 1 не существует", ex.getMessage());

        taskManager.addTask(task);
        final int taskId = task.getId();

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не возвращается.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Количество задач в истории не совпадает");
        assertEquals(task, history.get(0), "Задачи в истории не совпадают");
    }

    @Test
    void getEpic() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getEpic(1)
        );
        assertEquals("Эпика с id 1 не существует", ex.getMessage());

        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не возвращается.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Количество задач в истории не совпадает");
        assertEquals(epic, history.get(0), "Задачи в истории не совпадают");
    }

    @Test
    void getSubtask() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getSubtask(1)
        );
        assertEquals("Подзадачи с id 1 не существует", ex.getMessage());

        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        final int subtaskId = subtask.getId();

        final Task savedSubTask = taskManager.getSubtask(subtaskId);
        Integer savedEpicId = subtask.getEpicId();

        assertNotNull(savedSubTask, "Подзадача не возвращается.");
        assertEquals(subtask, savedSubTask, "Подзадачи не совпадают.");
        assertNotNull(savedEpicId, "Эпик не возвращается");
        assertEquals(savedEpicId, epicId, "Эпики не совпадают");

        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Количество задач в истории не совпадает");
        assertEquals(subtask, history.get(0), "Задачи в истории не совпадают");
    }

    @Test
    void addTask() {
        taskManager.addTask(task);
        final int taskId = task.getId();

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getTasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(1, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(task, prioritizedTasks.get(0), "Задачи не совпадают.");

        //-----задача с заданным временем-----//
        Task task2 = new Task("Task2", "Task2 description");
        task2.setStartTime(Optional.of(TEST_TIME));
        task2.setDuration(Optional.of(1L));
        taskManager.addTask(task2);
        final int taskId2 = task2.getId();

        final Task savedTask2 = taskManager.getTask(taskId2);

        assertNotNull(savedTask2, "Задача не найдена.");
        assertEquals(task2, savedTask2, "Задачи не совпадают.");

        tasks = taskManager.getTasks();
        prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(List.of(task, task2), tasks, "Задачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(2, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(List.of(task2, task), prioritizedTasks, " Задачи по приоритету не совпадают");

        //-----задача с пересечением по времени-----//
        Task task3 = new Task("Task3", "Task3 description");
        task3.setStartTime(Optional.of(TEST_TIME.minusMinutes(1L)));
        task3.setDuration(Optional.of(2L));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(task3)
        );
        assertEquals("Не более одной задачи за раз", ex.getMessage());
    }

    @Test
    void addEpic() {
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        ArrayList<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
        assertEquals(Status.NEW, epic.getStatus(), "Статусы не совпадают");
    }

    @Test
    void addSubtask() {
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        final int subtaskId = subtask.getId();

        final Task savedSubTask = taskManager.getSubtask(subtaskId);
        Integer savedEpicId = subtask.getEpicId();

        assertNotNull(savedSubTask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubTask, "Подзадачи не совпадают.");
        assertNotNull(savedEpicId, "Эпик не задан");
        assertEquals(savedEpicId, epicId, "Эпики не совпадают");

        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(1, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(subtask, prioritizedTasks.get(0), "Задачи не совпадают.");

        //-----подзадача с заданным временем-----//
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description");
        subtask2.setStartTime(Optional.of(TEST_TIME));
        subtask2.setDuration(Optional.of(1L));
        taskManager.addSubtask(subtask2, epicId);
        final int taskId2 = subtask2.getId();

        final Task savedSubtask2 = taskManager.getSubtask(taskId2);

        assertNotNull(savedSubtask2, "Задача не найдена.");
        assertEquals(subtask2, savedSubtask2, "Задачи не совпадают.");

        subtasks = taskManager.getSubtasks();
        prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(subtasks, "Задачи на возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество задач.");
        assertEquals(List.of(subtask, subtask2), subtasks, "Задачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(2, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(List.of(subtask2, subtask), prioritizedTasks, "Задачи не совпадают.");

        //-----подзадача с пересечением по времени-----//
        Subtask subtask3 = new Subtask("Subtask3", "Subtask3 description");
        subtask3.setStartTime(Optional.of(TEST_TIME.minusMinutes(1L)));
        subtask3.setDuration(Optional.of(2L));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addSubtask(subtask3, epicId)
        );
        assertEquals("Не более одной задачи за раз", ex.getMessage());
    }

    @Test
    void updateTask() {
        //id не задан
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateTask(task)
        );
        assertEquals("Можно обновить только добавленную задачу с присвоенным id", ex.getMessage());

        task.setId(1);

        //id нет в менеджере
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateTask(task)
        );
        assertEquals("Задачи с id 1 не существует", ex1.getMessage());

        //Обновление задачи: стандартное поведение
        taskManager.addTask(task);
        int taskId = task.getId();

        Task updatedTask = taskManager.getTask(taskId);
        updatedTask.setStatus(Status.DONE);
        updatedTask.setDuration(Optional.of(1L));
        updatedTask.setStartTime(Optional.of(TEST_TIME));
        taskManager.updateTask(updatedTask);
        Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(updatedTask, savedTask, "Задачи не совпадают");

        List<Task> tasks = taskManager.getTasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(savedTask, tasks.get(0), "Задачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(1, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(savedTask, prioritizedTasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void updateEpic() {
        //id не задан
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateEpic(epic)
        );
        assertEquals("Можно обновить только добавленный эпик с присвоенным id", ex.getMessage());

        epic.setId(1);

        //id нет в менеджере
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateEpic(epic)
        );
        assertEquals("Эпика с id 1 не существует", ex1.getMessage());

        //Обновление эпика: стандартное поведение
        taskManager.addEpic(epic);
        int epicId = epic.getId();

        Epic updatedEpic = taskManager.getEpic(epicId);
        updatedEpic.setDescription("New epic description");
        taskManager.updateEpic(updatedEpic);
        Task savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(updatedEpic, savedEpic, "Эпики не совпадают");

        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(savedEpic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void updateSubtask() {
        //id не задан
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateSubtask(subtask)
        );
        assertEquals("Можно обновить только добавленную подзадачу с присвоенным id", ex.getMessage());

        subtask.setId(1);

        //id нет в менеджере
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateSubtask(subtask)
        );
        assertEquals("Подзадачи с id 1 не существует", ex1.getMessage());

        //Обновление задачи: стандартное поведение
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        int taskId = subtask.getId();

        Subtask updatedSubtask = taskManager.getSubtask(taskId);
        updatedSubtask.setStatus(Status.DONE);
        updatedSubtask.setDuration(Optional.of(1L));
        updatedSubtask.setStartTime(Optional.of(TEST_TIME));
        taskManager.updateSubtask(updatedSubtask);
        Subtask savedSubtask = taskManager.getSubtask(taskId);
        Epic savedEpic = taskManager.getEpic(savedSubtask.getEpicId());
        Epic updatedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(updatedSubtask, savedSubtask, "Подзадачи не совпадают");

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(updatedEpic, savedEpic, "Эпики не совпадают");

        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(subtasks, "Задачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(savedSubtask, subtasks.get(0), "Задачи не совпадают.");

        assertNotNull(prioritizedTasks, "Задачи по приоритету на возвращаются.");
        assertEquals(1, prioritizedTasks.size(), "Неверное количество задач по приоритету.");
        assertEquals(savedSubtask, prioritizedTasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void removeTask() {
        //несуществующий id
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.removeTask(1)
        );
        assertEquals("Задачи с id 1 не существует", ex.getMessage());

        //удаление задачи, регулярное поведение
        taskManager.addTask(task);
        int taskId = task.getId();
        taskManager.getTask(task.getId());
        taskManager.removeTask(taskId);

        List<Task> tasks = taskManager.getTasks();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(tasks, "Список задач не возвращается");
        assertEquals(0, tasks.size(), "Список задач не пустой");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
    }

    @Test
    void removeEpic() {
        //несуществующий id
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.removeEpic(1)
        );
        assertEquals("Эпика с id 1 не существует", ex.getMessage());

        //удаление эпика, регулярное поведение
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        int subtaskId = subtask.getId();
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);

        taskManager.removeEpic(epicId);

        List<Epic> epics = taskManager.getEpics();
        List<Task> history = taskManager.getHistoryManager().getHistory();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(epics, "Список задач не возвращается");
        assertEquals(0, epics.size(), "Список задач не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
    }

    @Test
    void removeSubtask() {
        //несуществующий id
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.removeSubtask(1)
        );
        assertEquals("Подзадачи с id 1 не существует", ex.getMessage());

        //регулярное поведение
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        taskManager.addSubtask(subtask, epicId);
        int subtaskId = subtask.getId();
        taskManager.getSubtask(subtaskId);

        taskManager.removeSubtask(subtaskId);

        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Epic> epics = taskManager.getEpics();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> history = taskManager.getHistoryManager().getHistory();

        assertNotNull(subtasks, "Список подзадач не возвращается");
        assertEquals(0, subtasks.size(), "Список задач не пустой");
        assertNotNull(epics, "Список эпиков не возвращается");
        assertEquals(1, epics.size(), "Количество эпиков не совпадает");
        assertEquals(epic, epics.get(0), "Эпики не совпадают");
        assertNotNull(prioritizedTasks, "Список задач по приоритету не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Список задач по приоритету не пустой");
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "История не пустая");
    }
}