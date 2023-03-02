package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.*;
import service.FileBackedTasksManager;
import service.Managers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer server;
    private final Gson gson;
    private final FileBackedTasksManager taskManager;
    private final String pathFileManager = "src" + File.separator + "resources" + File.separator + "log.csv";
    private final String urlHttpManager = "http://localhost:8078";

    public HttpTaskServer() throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks/task", this::handleTasks);
        server.createContext("/tasks/epic", this::handleEpics);
        server.createContext("/tasks/subtask", this::handleSubtasks);
        server.createContext("/tasks/history", this::handleHistory);
        server.createContext("/tasks", this::handlePrioritizedTasks);
        gson = Managers.getGson();
        taskManager = Managers.getFileManager(pathFileManager);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
        Task task = new Task("Task", "Task description");
        Task newTask = new Task("Task2", "Task2 description");
        Epic epic = new Epic("Epic", "Epic description");
        Subtask subtask = new Subtask("Subtask", "Subtask description");

        server.taskManager.addTask(task);
        server.taskManager.addEpic(epic);
        int id = epic.getId();
        server.taskManager.addSubtask(subtask, id);

        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = Managers.getGson();
        String json = gson.toJson(newTask);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        newTask.setId(4);
        newTask.setStatus(Status.DONE);
        newTask.setDuration(Optional.of(30L));
        LocalDateTime TEST_TIME = LocalDateTime.of(2023, 1, 1, 10, 30);
        newTask.setStartTime(Optional.of(TEST_TIME));
        json = gson.toJson(newTask);
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        url = URI.create("http://localhost:8080/tasks/task/?id=4");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        url = URI.create("http://localhost:8080/tasks/task/?id=1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }

    public FileBackedTasksManager getTaskManager() {
        return taskManager;
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String method = exchange.getRequestMethod();
            String response;
            switch (method) {
                case "GET":
                    if (query == null) {
                        response = gson.toJson(taskManager.getTasks());
                        writeResponse(exchange, response, 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (taskManager.isTaskPresent(id)) {
                            response = gson.toJson(taskManager.getTask(id));
                            writeResponse(exchange, response, 200);
                            return;
                        } else {
                            writeResponse(exchange, "Задачи с id " + id + " не существует", 405);
                        }
                    } else {
                        writeResponse(exchange, "Запрос GET " + path + "?" + query + " не обрабатывается", 405);
                    }
                    break;
                case "POST":
                    handlePostTask(exchange);
                    break;
                case "DELETE":
                    if (query == null) {
                        taskManager.clearTasks();
                        writeResponse(exchange, "Все задачи удалены", 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (taskManager.isTaskPresent(id)) {
                            taskManager.removeTask(id);
                            writeResponse(exchange, "Задача с id " + id + " удалена", 200);
                        } else {
                            writeResponse(exchange, "Задачи с id " + id + " не существует", 405);
                        }
                        return;
                    } else {
                        writeResponse(exchange, "Запрос DELETE " + path + "?" + query + " не обрабатывается", 405);
                    }
                    break;
                default:
                    writeResponse(exchange, "Запросы " + method + " не обрабатываются", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEpics(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String method = exchange.getRequestMethod();
            String response;
            switch (method) {
                case "GET":
                    if (query == null) {
                        response = gson.toJson(taskManager.getEpics());
                        writeResponse(exchange, response, 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (taskManager.isEpicPresent(id)) {
                            response = gson.toJson(taskManager.getEpic(id));
                            writeResponse(exchange, response, 200);
                            return;
                        } else {
                            writeResponse(exchange, "Эпика с id " + id + " не существует", 405);
                        }
                    } else {
                        writeResponse(exchange, "Запрос GET " + path + "?" + query + " не обрабатывается", 405);
                    }
                    break;
                case "POST":
                    handlePostEpic(exchange);
                    break;
                case "DELETE":
                    if (query == null) {
                        taskManager.clearEpics();
                        writeResponse(exchange, "Все эпики удалены", 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (taskManager.isEpicPresent(id)) {
                            taskManager.removeEpic(id);
                            writeResponse(exchange, "Эпик с id " + id + " удален", 200);
                        } else {
                            writeResponse(exchange, "Эпика с id " + id + " не существует", 405);
                        }
                        return;
                    } else {
                        writeResponse(exchange, "Запрос DELETE " + path + "?" + query + " не обрабатывается", 405);
                    }
                    break;
                default:
                    writeResponse(exchange, "Запросы " + method + " не обрабатываются", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSubtasks(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String method = exchange.getRequestMethod();
            String response;
            switch (method) {
                case "GET":
                    if (query == null) {
                        response = gson.toJson(taskManager.getSubtasks());
                        writeResponse(exchange, response, 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (Pattern.matches("^/tasks/subtask/epic/$", path)) {
                            if (taskManager.isEpicPresent(id)) {
                                response = gson.toJson(taskManager.getEpicSubtasks(id));
                                writeResponse(exchange, response, 200);
                                return;
                            } else {
                                writeResponse(exchange, "Эпика с id " + id + " не существует", 405);
                            }
                        } else if (Pattern.matches("^/tasks/subtask/$", path)) {
                            if (taskManager.isSubtaskPresent(id)) {
                                response = gson.toJson(taskManager.getSubtask(id));
                                writeResponse(exchange, response, 200);
                                return;
                            } else {
                                writeResponse(exchange, "Подзадачи с id " + id + " не существует", 405);
                            }
                        } else writeResponse(exchange, "Запрос GET " + path + "?" + query + " не обрабатывается", 405);
                    } else {
                        writeResponse(exchange, "Запрос GET " + path + "?" + query + " не обрабатывается", 405);
                    }
                    break;
                case "POST":
                    handlePostSubtask(exchange);
                    break;
                case "DELETE":
                    if (query == null) {
                        taskManager.clearSubtasks();
                        writeResponse(exchange, "Все подзадачи удалены", 200);
                        return;
                    } else if (Pattern.matches("^id=\\d+$", query)) {
                        String pathId = query.replaceFirst("id=", "");
                        int id = parsePathId(pathId);
                        if (Pattern.matches("^/tasks/subtask/$", path)) {
                            if (taskManager.isSubtaskPresent(id)) {
                                taskManager.removeSubtask(id);
                                writeResponse(exchange, "Подзадача с id " + id + " удалена", 200);
                            } else {
                                writeResponse(exchange, "Подзадачи с id " + id + " не существует", 405);
                            }
                            return;
                        } else {
                            writeResponse(exchange, "Запрос DELETE " + path + "?" + query + " не обрабатывается", 405);
                        }
                    }
                    break;
                default:
                    writeResponse(exchange, "Запросы " + method + " не обрабатываются", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            String response = "";
            if ("GET".equals(method)) {
                response = gson.toJson(taskManager.getHistoryManager().getHistory());
                writeResponse(exchange, response, 200);
            } else {
                writeResponse(exchange, "/tasks/history ждёт GET-запрос, а получил: " + method + " — не обрабатывается", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePrioritizedTasks(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                String response = gson.toJson(taskManager.getPrioritizedTasks());
                writeResponse(exchange, response, 200);
            } else {
                writeResponse(exchange, "/tasks ждёт GET-запрос, а получил: " + method + " — не обрабатывается", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Task task;
        try {
            task = gson.fromJson(body, Task.class);
            if ((task.getTitle() == null) || (task.getDescription() == null)) {
                writeResponse(exchange, "Поля задачи не могут быть пустыми", 400);
                return;
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        int id = task.getId();
        if (id == 0) {
            taskManager.addTask(task);
            writeResponse(exchange, "Задача добавлена с id " + task.getId(), 201);
            return;
        } else if (taskManager.isTaskPresent(id)) {
            taskManager.updateTask(task);
            writeResponse(exchange, "Задача с id " + id + " обновлена", 201);
            return;
        }
        writeResponse(exchange, "Задача с id " + id + " не найдена", 404);
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Subtask task;
        try {
            task = gson.fromJson(body, Subtask.class);
            if ((task.getTitle() == null) || (task.getDescription() == null) || (task.getEpicId() == 0)) {
                writeResponse(exchange, "Поля подзадачи не могут быть пустыми", 400);
                return;
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        int id = task.getId();
        int epicId = task.getEpicId();
        if (!taskManager.isEpicPresent(epicId)) {
            writeResponse(exchange, "Эпик с id " + epicId + " не найден", 404);
            return;
        }
        if (id == 0) {
            taskManager.addSubtask(task, epicId);
            writeResponse(exchange, "Подзадача добавлена", 201);
            return;
        } else if (taskManager.isSubtaskPresent(id)) {
            taskManager.updateSubtask(task);
            writeResponse(exchange, "Подзадача обновлена", 201);
            return;
        }
        writeResponse(exchange, "Подзадача с id " + id + " не найдена", 404);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Epic epic;
        try {
            epic = gson.fromJson(body, Epic.class);
            if ((epic.getTitle() == null) || (epic.getDescription() == null)) {
                writeResponse(exchange, "Поля эпика не могут быть пустыми", 400);
                return;
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        int id = epic.getId();
        if (id == 0) {
            taskManager.addEpic(epic);
            writeResponse(exchange, "Эпик добавлен", 201);
            return;
        } else if (taskManager.isEpicPresent(id)) {
            taskManager.updateEpic(epic);
            writeResponse(exchange, "Эпик обновлен", 201);
            return;
        }
        writeResponse(exchange, "Эпик с id " + id + " не найден", 404);
    }

    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    protected int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public void start() {
        System.out.println("Started TaskServer " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер на порту " + PORT + " остановлен");
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }
}