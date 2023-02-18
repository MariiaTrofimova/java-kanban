package test;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {

    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Task task = new Task(1, "Task", "Task description");
    private final Epic epic = new Epic(2, "Epic", "Epic description");
    private final Subtask subtask = new Subtask(3, "Subtask", "Subtask description", 2);

    @Test
    void add() {
        //Пустая история задач
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверное количество задач.");

        //Дублирование с одним элементом
        historyManager.add(task);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверное количество задач.");

        //стандартный кейс
        historyManager.add(epic);
        historyManager.add(subtask);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(3, history.size(), "Неверное количество задач.");
        int[] testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{1, 2, 3}, testHistory, "Элементы не совпадают");

        //Дублирование
        historyManager.add(task);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(3, history.size(), "Неверное количество задач.");
        testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{2, 3, 1}, testHistory, "Элементы не совпадают");
    }

    @Test
    void remove() {
        //Пустая история задач
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> historyManager.remove(1)
        );
        assertEquals("Задачи с id 1 нет в истории просмотров", ex.getMessage());

        //Удаление из истории: начало
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(2, history.size(), "Неверное количество задач.");
        int[] testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{2, 3}, testHistory, "Элементы не совпадают");

        //Удаление из истории: конец
        historyManager.remove(3);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверное количество задач.");
        assertEquals(2, history.get(0).getId());

        //Удаление из истории: середина
        historyManager.add(task);
        historyManager.add(subtask);
        historyManager.remove(1);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(2, history.size(), "Неверное количество задач.");
        testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{2, 3}, testHistory, "Элементы не совпадают");
    }

    @Test
    void getHistory() {
        //Пустая история задач
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "История не пустая.");

        //стандартный кейс
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(3, history.size(), "Неверное количество задач.");
        int[] testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{1, 2, 3}, testHistory, "Элементы не совпадают");

        //Дублирование
        historyManager.add(task);
        history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(3, history.size(), "Неверное количество задач.");
        testHistory = history.stream()
                .mapToInt(Task::getId)
                .toArray();
        assertArrayEquals(new int[]{2, 3, 1}, testHistory, "Элементы не совпадают");
    }
}