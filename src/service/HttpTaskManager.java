package service;

import com.google.gson.Gson;

import client.KVTaskClient;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    //вместо имени файла принимать URL к серверу KVServer
    private final KVTaskClient client;
    private final Gson gson;

    public HttpTaskManager(String url) throws IOException, InterruptedException {
        super(url);
        client = new KVTaskClient(url);
        gson = Managers.getGson();
    }

    @Override
    public void save() {
        client.put("tasks", gson.toJson(getTasks()));
        client.put("epics", gson.toJson(getEpics()));
        client.put("subtasks", gson.toJson(getSubtasks()));
        //client.put("history", gson.toJson(historyManager.getHistory()));
        client.put("history", gson.toJson(historyManager.getHistory().stream()
                .map(Task::getId)
                .collect(Collectors.toList())));
    }

    public HttpTaskManager loadFromServer(String url) throws IOException, InterruptedException {
        HttpTaskManager managerFromServer = new HttpTaskManager(url);
        HistoryManager historyManager = managerFromServer.getHistoryManager();
        int maxId = 0;

        String jsonString = client.load("tasks");
        if (!jsonString.isEmpty()) {
            Type tasksListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(jsonString, tasksListType);
            for (Task task : tasks) {
                managerFromServer.tasks.put(task.getId(), task);
                managerFromServer.prioritizedTasks.add(task);
                managerFromServer.fillTimeSlots(task);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
            }
        }
        jsonString = client.load("epics");
        if (!jsonString.isEmpty()) {
            Type epicsListType = new TypeToken<List<Epic>>() {}.getType();
            List<Epic> epics = gson.fromJson(jsonString, epicsListType);
            for (Epic epic : epics) {
                managerFromServer.epics.put(epic.getId(), epic);
                if (epic.getId() > maxId) {
                    maxId = epic.getId();
                }
            }
        }
        jsonString = client.load("subtasks");
        if (!jsonString.isEmpty()) {
            Type subtasksListType = new TypeToken<List<Subtask>>() {}.getType();
            List<Subtask> subtasks = gson.fromJson(jsonString, subtasksListType);
            for (Subtask subtask : subtasks) {
                managerFromServer.subtasks.put(subtask.getId(), subtask);
                managerFromServer.prioritizedTasks.add(subtask);
                managerFromServer.fillTimeSlots(subtask);
                if (subtask.getId() > maxId) {
                    maxId = subtask.getId();
                }
            }
        }
        jsonString = client.load("history");
        if (!jsonString.isEmpty()) {
            //Type tasksListType = new TypeToken<List<Task>>() {}.getType();
            Type tasksListType = new TypeToken<List<Integer>>() {}.getType();
            List<Integer> history = gson.fromJson(jsonString, tasksListType);
            for (Integer id : history) {
                if (managerFromServer.epics.containsKey(id)) {
                    historyManager.add(managerFromServer.epics.get(id));
                } else if (managerFromServer.subtasks.containsKey(id)) {
                    historyManager.add(managerFromServer.subtasks.get(id));
                } else if (managerFromServer.tasks.containsKey(id)) {
                    historyManager.add(managerFromServer.tasks.get(id));
                }
                //historyManager.add(task);
            }
        }

        managerFromServer.nextId = maxId + 1;

        return managerFromServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HttpTaskManager that = (HttpTaskManager) o;
        return Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), client);
    }
}
