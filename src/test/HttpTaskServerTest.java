package test;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.Managers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {

    private HttpTaskServer server;
    protected static final LocalDateTime TEST_TIME =
            LocalDateTime.of(2023, 1, 1, 10, 30);
    private final Gson gson= Managers.getGson();

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldHandleTasks() throws IOException, InterruptedException {
        Task task = new Task("Task", "Task description");
        //POST /tasks/task/Body:{task…}
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpClient client = HttpClient.newHttpClient();
        String json = gson.toJson(task);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/task/
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type tasksListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(response.body(), tasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        task.setId(1);
        assertEquals(task,tasks.get(0), "Задачи не совпадают");

        //POST /tasks/task/Body:{task…} --> обновление задачи
        task.setStatus(Status.DONE);
        task.setDuration(Optional.of(30L));
        task.setStartTime(Optional.of(TEST_TIME));

        json = gson.toJson(task);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/task/?id=
        url = URI.create("http://localhost:8080/tasks/task/?id=1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task taskAdded = gson.fromJson(response.body(), Task.class);
        assertEquals(200, response.statusCode());
        assertNotNull(taskAdded, "Задача не возвращается");
        assertEquals(task,taskAdded, "Задачи не совпадают");

        //GET /tasks/history
        List<Task> history = getHistory(client);

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Неверное количество задач в истории");
        assertEquals(task, history.get(0), "Задачи не совпадают");

        // GET /tasks → getPrioritizedTasks/
        Task task2 = new Task("Task2", "Task2 description");
        url = URI.create("http://localhost:8080/tasks/task/");
        json = gson.toJson(task2);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> prioritizedTasks = getPrioritizedTasks(client);

        assertNotNull(prioritizedTasks, "Задачи не возвращаются");
        assertEquals(2, prioritizedTasks.size(), "Неверное количество задач");
        assertEquals(task,prioritizedTasks.get(0), "Задачи не совпадают");

        //DELETE /tasks/task/?id=
        url = URI.create("http://localhost:8080/tasks/task/?id=1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        url = URI.create("http://localhost:8080/tasks/task/");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        tasks = gson.fromJson(response.body(), tasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");

        //DELETE /tasks/task/
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        tasks = gson.fromJson(response.body(), tasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(0, tasks.size(), "Неверное количество задач");
    }

    @Test
    void shouldHandleEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        Epic epic2 = new Epic("Epic2", "Epic2 description");

        //POST /tasks/epic/Body:{epic…}
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpClient client = HttpClient.newHttpClient();
        String json = gson.toJson(epic);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/epic/
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type epicsListType = new TypeToken<List<Epic>>() {}.getType();
        List<Epic> epics = gson.fromJson(response.body(), epicsListType);

        assertEquals(200, response.statusCode());

        assertNotNull(epics, "Задачи не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество задач");
        epic.setId(1);
        assertEquals(epic,epics.get(0), "Задачи не совпадают");

        //POST /tasks/epic/Body:{task…} --> обновление задачи
        epic.setTitle("Updated Epic");

        json = gson.toJson(epic);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        System.out.println(response.statusCode());

        assertEquals(201, response.statusCode());

        //GET /tasks/epic/?id=
        url = URI.create("http://localhost:8080/tasks/epic/?id=1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic epicAdded = gson.fromJson(response.body(), Epic.class);
        assertEquals(200, response.statusCode());
        assertNotNull(epicAdded, "Эпик не возвращается");
        assertEquals(epic,epicAdded, "Эпикии не совпадают");

        //GET /tasks/history
        List<Task> history = getHistory(client);

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Неверное количество задач в истории");
        assertEquals(epic, history.get(0), "Задачи не совпадают");

        //POST /tasks/epic/Body:{epic…}
        url = URI.create("http://localhost:8080/tasks/epic/");
        json = gson.toJson(epic2);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //DELETE /tasks/epic/?id=
        url = URI.create("http://localhost:8080/tasks/epic/?id=1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        url = URI.create("http://localhost:8080/tasks/epic/");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epics = gson.fromJson(response.body(), epicsListType);

        assertEquals(200, response.statusCode());

        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество эпиков");

        //DELETE /tasks/task/
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epics = gson.fromJson(response.body(), epicsListType);

        assertEquals(200, response.statusCode());

        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(0, epics.size(), "Неверное количество эпиков");
    }

    @Test
    void shouldHandleSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        Subtask subtask = new Subtask("Subtask", "Subtask description");
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description");

        //POST /tasks/epic/Body:{epic…}
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpClient client = HttpClient.newHttpClient();
        String json = gson.toJson(epic);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //POST /tasks/subtask/Body:{epic…}
        url = URI.create("http://localhost:8080/tasks/subtask/");
        subtask.setEpicId(1);
        json = gson.toJson(subtask);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/subtask/
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type subtasksListType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), subtasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач");
        subtask.setId(2);
        assertEquals(subtask,subtasks.get(0), "Подзадачи не совпадают");

        //POST /tasks/task/Body:{task…} --> обновление задачи
        subtask.setStatus(Status.DONE);
        subtask.setDuration(Optional.of(30L));
        subtask.setStartTime(Optional.of(TEST_TIME));

        json = gson.toJson(subtask);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/subtask/?id=
        url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask subtaskAdded = gson.fromJson(response.body(), Subtask.class);
        assertEquals(200, response.statusCode());
        assertNotNull(subtaskAdded, "Подзадача не возвращается");
        assertEquals(subtask,subtaskAdded, "Подзадачи не совпадают");

        //GET /tasks/history
        List<Task> history = getHistory(client);

        assertNotNull(history, "История не возвращается");
        assertEquals(1, history.size(), "Неверное количество задач в истории");
        assertEquals(subtask, history.get(0), "Задачи не совпадают");

        // Две подзадачи в мапе и в истории
        url = URI.create("http://localhost:8080/tasks/subtask/");
        subtask2.setEpicId(1);
        json = gson.toJson(subtask2);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        //GET /tasks/subtask/?id=
        url = URI.create("http://localhost:8080/tasks/subtask/?id=3");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //GET /tasks/history
        history = getHistory(client);

        assertNotNull(history, "История не возвращается");
        assertEquals(2, history.size(), "Неверное количество задач в истории");

        //GET /tasks → getPrioritizedTasks/
        List<Task> prioritizedTasks = getPrioritizedTasks(client);

        assertNotNull(prioritizedTasks, "Задачи не возвращаются");
        assertEquals(2, prioritizedTasks.size(), "Неверное количество задач");
        assertEquals(subtask,prioritizedTasks.get(0), "Задачи не совпадают");

        //GET /tasks/subtask/epic/?id=!!!!!!!!!!!!
        url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Subtask> epicSubtasks = gson.fromJson(response.body(), subtasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(epicSubtasks, "Подзадачи не возвращаются");
        assertEquals(2, epicSubtasks.size(), "Неверное количество подзадач");
        assertEquals(subtask,epicSubtasks.get(0), "Подзадачи в эпике не совпадают");

        //DELETE /tasks/subtask/?id=
        url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        url = URI.create("http://localhost:8080/tasks/subtask/");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        subtasks = gson.fromJson(response.body(), subtasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач");

        //DELETE /tasks/subtask/
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        subtasks = gson.fromJson(response.body(), subtasksListType);

        assertEquals(200, response.statusCode());

        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(0, subtasks.size(), "Неверное количество подзадач");

    }

    @Test
    void shouldHandleHistory() throws IOException, InterruptedException {
        //пустой список задач, остальные случаи в других тестах
        HttpClient client = HttpClient.newHttpClient();
        List<Task> history = getHistory(client);

        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.size(), "Количество задач в истории не совпадает");
    }

    @Test
    void shouldHandlePrioritizedTasks() throws IOException, InterruptedException {
        //пустой список задач, остальные случаи в других тестах
        HttpClient client = HttpClient.newHttpClient();
        List<Task> prioritizedTasks = getPrioritizedTasks(client);

        assertNotNull(prioritizedTasks, "История не возвращается");
        assertEquals(0, prioritizedTasks.size(), "Количество задач в истории не совпадает");
    }

    List<Task> getDiffTasks(HttpClient client, URI url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body()) ;

        assertEquals(200, response.statusCode());

        List<Task> tasks = new ArrayList<>();
        Type taskType = new TypeToken<Task>() {}.getType();
        Type subtaskType = new TypeToken<Subtask>() {}.getType();
        Type epicType = new TypeToken<Epic>() {}.getType();

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String itemType = jsonObject.get("taskType").getAsString();
            if (itemType.equals (TaskType.TASK.toString())) tasks .add((gson. fromJson (jsonObject, taskType)));
            if (itemType.equals (TaskType.SUBTASK.toString())) tasks .add((gson. fromJson (jsonObject, subtaskType)));
            if (itemType.equals (TaskType.EPIC.toString())) tasks.add((gson. fromJson (jsonObject, epicType)));
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray() ;
            for (JsonElement element : jsonArray) {
                String itemType = element.getAsJsonObject().get("taskType").getAsString();
                if (itemType.equals (TaskType.TASK.toString())) tasks .add((gson. fromJson (element, taskType)));
                if (itemType.equals (TaskType.SUBTASK.toString())) tasks .add((gson. fromJson (element, subtaskType)));
                if (itemType.equals (TaskType.EPIC.toString())) tasks .add((gson. fromJson (element, epicType)));
            }
        }
        return tasks;
    }

    List<Task> getHistory(HttpClient client) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/history");
        return getDiffTasks(client, url);
    }

    List<Task> getPrioritizedTasks(HttpClient client) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/");
        return getDiffTasks(client, url);
    }
}