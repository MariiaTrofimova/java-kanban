package test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.KVServer;
import service.HttpTaskManager;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    private final String url = "http://localhost:8078";
    private static KVServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new KVServer();
        server.start();
    }

    @Override
    void setTaskManager() throws IOException, InterruptedException {
        taskManager = new HttpTaskManager(url);
    }

    @Test
    void save() throws IOException, InterruptedException {
        taskManager.addTask(task);
        HttpTaskManager taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.addEpic(epic);
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        int epicId = epic.getId();

        subtask.setStartTime(Optional.of(TEST_TIME));
        subtask.setDuration(Optional.of(1L));
        taskManager.addSubtask(subtask, epicId);

        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.getTask(task.getId());
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.getTask(task.getId());
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.getSubtask(subtask.getId());
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.getEpic(epic.getId());
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");

        taskManager.removeTask(1);
        taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Состояние не записано");
        assertEquals(taskManager, taskManagerFromServer, "Менеджеры не совпадают");
    }

    @Test
    void loadFromServer() throws IOException, InterruptedException {
        //все тесты пересекаются с save()
        //пустой менеджер
        HttpTaskManager taskManagerFromServer = HttpTaskManager.loadFromServer(url);
        assertNotNull(taskManagerFromServer, "Менеджер не загружается");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }
}