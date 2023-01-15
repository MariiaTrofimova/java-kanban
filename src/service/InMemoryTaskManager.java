package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public ArrayList<Task> getTasksList() {
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
        }
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Task> getEpicsList() {
        if (epics.isEmpty()) {
            System.out.println("Список эпиков пуст");
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Task> getSubtasksList() {
        if (subtasks.isEmpty()) {
            System.out.println("Список подзадач пуст");
        }
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Эпика с id " + id + " не существует");
            return null;
        }
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        ArrayList<Integer> epicSubtasksIds = epics.get(id).getSubtaskIds();
        for (Integer subtaskId : epicSubtasksIds) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    private void setEpicStatus(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        Status status = epic.getStatus();

        switch (subtask.getStatus()) {
            case IN_PROGRESS:
                epic.setStatus(Status.IN_PROGRESS);
                return;
            case NEW:
                if (status == Status.DONE) {
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            case DONE:
                if ((status == Status.NEW) || (status == Status.IN_PROGRESS)) {
                    if (getEpicSubtasks(epicId).size() == 1) {
                        epic.setStatus(Status.DONE);
                        return;
                    }
                    for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                        if ((epicSubtask.getStatus() == Status.NEW)
                                || (epicSubtask.getStatus() == Status.IN_PROGRESS)) {
                            epic.setStatus(Status.IN_PROGRESS);
                            return;
                        }
                    }
                }
                break;
            default:
                System.out.println("Некорректный статус");
        }
    }

    private void calculateEpicStatus(int id) {
        if (getEpicSubtasks(id).isEmpty()) {
            epics.get(id).setStatus(Status.NEW);
            return;
        }
        for (Subtask epicSubtask : getEpicSubtasks(id)) {
            setEpicStatus(epicSubtask);
        }
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
    }

    @Override
    public Task getTask(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Задачи с id " + id + " не существует");
            return null;
        }
        //отметить задачу просмотренной
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Эпика с id " + id + " не существует");
            return null;
        }
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Подзадачи с id " + id + " не существует");
            return null;
        }
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void createTask(Task task) {
        task.setId(nextId++);
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask, int epicId) {
        subtask.setId(nextId++);
        subtask.setEpicId(epicId);
        subtasks.put(subtask.getId(), subtask);
        epics.get(epicId).addSubtask(subtask.getId()); //добавить номер подзадачи в epic
        //обновить статус epic
        if (epics.get(epicId).getStatus() == Status.DONE) {
            epics.get(epicId).setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Задачи с id " + task.getId() + " не существует");
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Эпика с id " + epic.getId() + " не существует");
            return;
        }
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            System.out.println("Подзадачи с id " + subtask.getId() + " не существует");
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        setEpicStatus(subtask); // обновление статуса эпика
    }

    @Override
    public void removeTask(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Задачи с id " + id + " не существует");
            return;
        }
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Эпика с id " + id + " не существует");
            return;
        }
        for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Подзадачи с id " + id + " не существует");
            return;
        }
        int epicId = getSubtask(id).getEpicId();
        subtasks.remove(id);
        epics.get(epicId).removeSubtaskId(id);
        calculateEpicStatus(epicId);
        historyManager.remove(id);
    }
}
