package test;

import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private static final LocalDateTime TEST_TIME =
            LocalDateTime.of(2023, 1, 1, 10, 30);

    @BeforeEach
    void createSubtasksAndEpic() {
        subtasks.clear();
        subtasks.put(1, new Subtask(1, "Subtask#1", "Subtask#1 description", Status.NEW, 0));
        subtasks.put(2, new Subtask(2, "Subtask#2", "Subtask#2 description", Status.NEW, 0));
    }

    @Test
    void setStatusBySubtask() {
        Epic epic = new Epic(0, "Epic#1", " Epic#1 description");
        for (int i = 1; i <= 2; i++) {
            epic.addSubtask(i);
        }
        //все подзадачи со статусом NEW
        epic.setStatusBySubtask(subtasks.get(1), subtasks);
        assertEquals(Status.NEW, epic.getStatus(), "Статусы не совпадают.");
        //подзадачи со статусами NEW и DONE
        subtasks.get(1).setStatus(Status.DONE);
        epic.setStatusBySubtask(subtasks.get(1), subtasks);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статусы не совпадают.");
        //все подзадачи со статусом DONE
        subtasks.get(2).setStatus(Status.DONE);
        epic.setStatusBySubtask(subtasks.get(1), subtasks);
        assertEquals(Status.DONE, epic.getStatus(), "Статусы не совпадают.");
        //подзадачи со статусом IN_PROGRESS
        subtasks.get(1).setStatus(Status.IN_PROGRESS);
        epic.setStatusBySubtask(subtasks.get(1), subtasks);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статусы не совпадают.");
    }

    @Test
    void setStatus() {
        Epic epic = new Epic(0, "Epic#1", " Epic#1 description");
        //пустой список подзадач
        assertEquals(Status.NEW, epic.getStatus(), "Статусы не совпадают.");
        //все подзадачи со статусом NEW
        for (int i = 1; i <= 2; i++) {
            epic.addSubtask(i);
        }
        epic.setStatus(subtasks);
        assertEquals(Status.NEW, epic.getStatus(), "Статусы не совпадают.");
        //подзадачи со статусами NEW и DONE
        subtasks.get(1).setStatus(Status.DONE);
        epic.setStatus(subtasks);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статусы не совпадают.");
        //все подзадачи со статусом DONE
        subtasks.get(2).setStatus(Status.DONE);
        epic.setStatus(subtasks);
        assertEquals(Status.DONE, epic.getStatus(), "Статусы не совпадают.");
        //подзадачи со статусом IN_PROGRESS
        subtasks.get(1).setStatus(Status.IN_PROGRESS);
        epic.setStatus(subtasks);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статусы не совпадают.");
    }

    @Test
    void setStartTime() {
        Epic epic = new Epic(0, "Epic#1", " Epic#1 description");
        //пустой список подзадач
        epic.setStartTime(subtasks);
        assertEquals(Optional.empty(), epic.getStartTime(), "Время старта не совпадает");
        //задача без заданного времени
        epic.addSubtask(1);
        epic.setStartTime(subtasks);
        assertEquals(Optional.empty(), epic.getStartTime(), "Время старта не совпадает");
        //задача с заданным временем
        subtasks.get(1).setStartTime(Optional.of(TEST_TIME));
        epic.setStartTime(subtasks);
        assertEquals(Optional.of(TEST_TIME), epic.getStartTime(), "Время старта не совпадает");
        // две задачи с заданным временем
        epic.addSubtask(2);
        subtasks.get(2).setStartTime(Optional.of(TEST_TIME.minusHours(1L)));
        epic.setStartTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.minusHours(1L)), epic.getStartTime(), "Время старта не совпадает");
    }

    @Test
    void setEndTime() {
        Epic epic = new Epic(0, "Epic#1", " Epic#1 description");
        //пустой список подзадач
        epic.setEndTime(subtasks);
        assertEquals(Optional.empty(), epic.getEndTime(), "Время завершения не совпадает");
        //подзадача без заданного времени начала
        epic.addSubtask(1);
        epic.setEndTime(subtasks);
        assertEquals(Optional.empty(), epic.getEndTime(), "Время завершения не совпадает");
        //подзадача с временем начала, без продолжительности
        subtasks.get(1).setStartTime(Optional.of(TEST_TIME));
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME), epic.getEndTime(), "Время завершения не совпадает");
        //подзадача с временем начала и с продолжительностью
        subtasks.get(1).setDuration(Optional.of(1L));
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.plusMinutes(1L)), epic.getEndTime(), "Время завершения не совпадает");
        //две задачи: одна с данными, другая без
        epic.addSubtask(2);
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.plusMinutes(1L)), epic.getEndTime(), "Время завершения не совпадает");
        //две задачи: одна с данными, другая только с временем начала раньше окончания другой
        subtasks.get(2).setStartTime(Optional.of(TEST_TIME.minusHours(1L)));
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.plusMinutes(1L)), epic.getEndTime(), "Время завершения не совпадает");
        //две задачи: одна с данными, другая только с временем начала позже окончания другой
        subtasks.get(2).setStartTime(Optional.of(TEST_TIME.plusHours(1L)));
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.plusHours(1L)), epic.getEndTime(), "Время завершения не совпадает");
        //две задачи с полными данными
        subtasks.get(2).setDuration(Optional.of(1L));
        epic.setEndTime(subtasks);
        assertEquals(Optional.of(TEST_TIME.plusMinutes(61L)), epic.getEndTime(), "Время завершения не совпадает");
    }

    @Test
    void setDuration() {
        Epic epic = new Epic(0, "Epic#1", " Epic#1 description");
        //пустой список подзадач
        epic.setDuration(subtasks);
        assertEquals(Optional.of(0L), epic.getDuration(), "Продолжительность не совпадает");
        //задача без заданной длительности
        epic.addSubtask(1);
        epic.setDuration(subtasks);
        assertEquals(Optional.of(0L), epic.getDuration(), "Продолжительность не совпадает");
        //задача с заданной длительностью
        subtasks.get(1).setDuration(Optional.of(1L));
        epic.setDuration(subtasks);
        assertEquals(Optional.of(1L), epic.getDuration(), "Продолжительность не совпадает");
        //две задачи: одна с duration, другая — без
        epic.addSubtask(2);
        epic.setDuration(subtasks);
        assertEquals(Optional.of(1L), epic.getDuration(), "Продолжительность не совпадает");
        //две задачи с duration
        subtasks.get(2).setDuration(Optional.of(1L));
        epic.setDuration(subtasks);
        assertEquals(Optional.of(2L), epic.getDuration(), "Продолжительность не совпадает");
    }
}