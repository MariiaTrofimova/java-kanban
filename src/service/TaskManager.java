package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    public int idGenerator = 0;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public ArrayList<Task> getTasksList() {
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
            return null;
        }
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Task> getEpicsList() {
        if (epics.isEmpty()) {
            System.out.println("Список эпиков пуст");
            return null;
        }
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Task> getSubtasksList() {
        if (subtasks.isEmpty()) {
            System.out.println("Список подзадач пуст");
            return null;
        }
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(int id) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        ArrayList<Integer> epicSubtasksIds = getEpic(id).getSubtaskIds();
        for (Integer subtaskId : epicSubtasksIds) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    public void setEpicStatus(Subtask subtask) {
        Status status = getEpic(subtask.getEpicId()).getStatus();
        int epicId = subtask.getEpicId();

        switch (subtask.getStatus()) {
            case IN_PROGRESS:
                getEpic(epicId).setStatus(Status.IN_PROGRESS);
                return;
            case NEW:
                if (status == Status.DONE) {
                    getEpic(epicId).setStatus(Status.IN_PROGRESS);
                    return;
                }
            case DONE:
                if ((status == Status.NEW) || (status == Status.IN_PROGRESS)) {
                    if (getEpicSubtasks(epicId).size() == 1) {
                        getEpic(epicId).setStatus(Status.DONE);
                        return;
                    }
                    for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                        if ((epicSubtask.getStatus() == Status.NEW)
                                || (epicSubtask.getStatus() == Status.IN_PROGRESS)) {
                            getEpic(subtask.getEpicId()).setStatus(Status.IN_PROGRESS);
                            return;
                        }
                    }
                }
                break;
            default:
                System.out.println("Некорректный статус");
        }
    }

    public void calculateEpicStatus(int id) {
        if (getEpicSubtasks(id).isEmpty()) {
            getEpic(id).setStatus(Status.NEW);
            return;
        }
        for (Subtask epicSubtask : getEpicSubtasks(id)) {
            setEpicStatus(epicSubtask);
        }
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public Task getTask(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Задачи с таким id не существует");
            return null;
        }
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Эпика с таким id не существует");
            return null;
        }
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Подзадачи с таким id не существует");
            return null;
        }
        return subtasks.get(id);
    }

    public void createTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void createSubtask(Subtask subtask, int epicId) {
        subtask.setEpicId(epicId);
        subtasks.put(subtask.getId(), subtask);
        getEpic(epicId).addSubtask(subtask.getId());
        //добавить номер подзадачи в epic

        //обновить статус epic
        if (getEpic(subtask.getEpicId()).getStatus() == Status.DONE) {
            getEpic(subtask.getEpicId()).setStatus(Status.IN_PROGRESS);
        }
    }

    public void updateTask(Task task) {
        //проверка на существование
        if (!subtasks.containsKey(task.getId())) {
            System.out.println("Задачи с таким id не существует");
            return;
        }
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        //проверка на существование
        if (!subtasks.containsKey(epic.getId())) {
            System.out.println("Эпика с таким id не существует");
            return;
        }
        epics.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {
        //проверка на существование
        if (!subtasks.containsKey(subtask.getId())) {
            System.out.println("Подзадачи с таким id не существует");
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        setEpicStatus(subtask); // обновление статуса эпика
    }

    public void updateTaskStatus(int id, Status status) {
        getTask(id).setStatus(status);
    }

    public void updateSubtaskStatus(int id, Status status) {
        getSubtask(id).setStatus(status);
        setEpicStatus(getSubtask(id));
    }

    public void removeTask(int id) {
        //проверка существования
        if (!tasks.containsKey(id)) {
            System.out.println("Задачи с таким id не существует");
            return;
        }
        tasks.remove(id);
    }

    public void removeEpic(int id) {
        //проверка существования
        if (!epics.containsKey(id)) {
            System.out.println("Эпика с таким id не существует");
            return;
        }
        for (Integer subtaskId : getEpic(id).getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        //проверка существования
        if (!subtasks.containsKey(id)) {
            System.out.println("Подзадачи с таким id не существует");
            return;
        }
        int epicId = getSubtask(id).getEpicId();

        subtasks.remove(id);
        getEpic(epicId).removeSubtaskId(id);
        calculateEpicStatus(epicId);
    }

}
