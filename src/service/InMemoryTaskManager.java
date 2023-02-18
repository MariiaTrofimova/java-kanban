package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();

    protected HistoryManager historyManager = Managers.getDefaultHistory();

    //список задач и подзадач с сортировкой по startTime
    //Если дата старта не задана--> в конец списка задач
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime().isPresent() && t2.getStartTime().isPresent()) {
            return t1.getStartTime().get().compareTo(t2.getStartTime().get());
        } else if (t1.getStartTime().isPresent()) {
            return -1;
        } else if (t2.getStartTime().isPresent()) {
            return 1;
        } else {
            return t1.getId() - t2.getId();
        }
    });

    //таблица занятости времени
    protected final Map<LocalDateTime, Boolean> taskTimeBusiness = Stream.iterate(0, i -> i + 15)
            .limit(365 * 24 * 60 / 15)
            .map(i -> LocalDateTime.of(2023, 1, 1, 0, 0).plusMinutes((long) i))
            .collect(Collectors.toMap(Function.identity(), t -> true));

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public ArrayList<Task> getTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
        }
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        if (epics.isEmpty()) {
            System.out.println("Список эпиков пуст");
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        if (subtasks.isEmpty()) {
            System.out.println("Список подзадач пуст");
        }
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпика с id " + id + " не существует");
        }
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        ArrayList<Integer> epicSubtasksIds = epics.get(id).getSubtaskIds();
        for (Integer subtaskId : epicSubtasksIds) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        if (prioritizedTasks.isEmpty()) {
            System.out.println("Список задач по приоритету пуст");
        }
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public void clearTasks() {
        for (Integer id : tasks.keySet()) {
            removeTask(id);
        }
    }

    @Override
    public void clearEpics() {
        clearSubtasks();
        for (Integer id : epics.keySet()) {
            removeEpic(id);
        }
    }

    @Override
    public void clearSubtasks() {
        for (Integer id : subtasks.keySet()) {
            removeSubtask(id);
        }
    }

    @Override
    public Task getTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задачи с id " + id + " не существует");
        }
        //отметить задачу просмотренной
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпика с id " + id + " не существует");
        }
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадачи с id " + id + " не существует");
        }
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    protected boolean isTimeFree(Task task) {
        boolean isTimeFree = true;
        if (task.getStartTime().isPresent()) {
            //валидация времени
            LocalDateTime startTimeCell =
                    task.getStartTime().get().minusMinutes(task.getStartTime().get().getMinute()
                            % 15).minusSeconds(task.getStartTime().get().getSecond());
            int count = (int) (Duration.between(startTimeCell, task.getEndTime().get()).toMinutes() / 15 + 1);
            List<LocalDateTime> busyTimeInterval = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (!taskTimeBusiness.get(startTimeCell)) {
                    busyTimeInterval.add(startTimeCell);
                    isTimeFree = false;
                }
                startTimeCell = startTimeCell.plusMinutes(15L);
            }
            //занять слоты, если свободны
            if (isTimeFree) {
                for (int i = 0; i < count; i++) {
                    startTimeCell = startTimeCell.minusMinutes(15L);
                    taskTimeBusiness.put(startTimeCell, false);
                }
            } else {
                System.out.println("Промежуток времени c " + busyTimeInterval.get(0).format(Task.formatter) + " по "
                        + busyTimeInterval.get(busyTimeInterval.size() - 1).plusMinutes(15).format(Task.formatter)
                        + " занят.\nПожалуйста, введите другое время для задачи.");
            }
        }
        return isTimeFree;
    }

    protected void clearTimeSlots(Task task) {
        if (task.getStartTime().isPresent()) {
            LocalDateTime startTimeCell =
                    task.getStartTime().get().minusMinutes(task.getStartTime().get().getMinute()
                            % 15).minusSeconds(task.getStartTime().get().getSecond());
            int count = (int) (Duration.between(startTimeCell, task.getEndTime().get()).toMinutes() / 15 + 1);
            for (int i = 0; i < count; i++) {
                taskTimeBusiness.put(startTimeCell, true);
                startTimeCell = startTimeCell.plusMinutes(15L);
            }
        }
    }

    protected void fillTimeSlots(Task task) {
        if (task.getStartTime().isPresent()) {
            LocalDateTime startTimeCell =
                    task.getStartTime().get().minusMinutes(task.getStartTime().get().getMinute()
                            % 15).minusSeconds(task.getStartTime().get().getSecond());
            int count = (int) (Duration.between(startTimeCell, task.getEndTime().get()).toMinutes() / 15 + 1);
            for (int i = 0; i < count; i++) {
                taskTimeBusiness.put(startTimeCell, false);
                startTimeCell = startTimeCell.plusMinutes(15L);
            }
        }
    }

    @Override
    public void addTask(Task task) {
        //валидация на свободный слот
        if (isTimeFree(task)) {
            task.setId(nextId++);
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            throw new IllegalArgumentException("Не более одной задачи за раз");
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask, int epicId) {
        if (isTimeFree(subtask)) {
            Epic epic = epics.get(epicId);
            subtask.setId(nextId++);
            subtask.setEpicId(epicId);
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
            //Обновление эпика
            epic.addSubtask(subtask.getId());
            if (epic.getStatus() == Status.DONE) {
                epic.setStatus(Status.IN_PROGRESS);
            }
            if (subtask.getStartTime().isPresent() || subtask.getDuration().isPresent()) {
                epic.setStartTime(subtasks);
                epic.setEndTime(subtasks);
                epic.setDuration(subtasks);
            }
        } else {
            throw new IllegalArgumentException("Не более одной задачи за раз");
        }
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (id == 0) {
            throw new IllegalArgumentException("Можно обновить только добавленную задачу с присвоенным id");
        }
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задачи с id " + id + " не существует");
        }
        //чистим старые слоты по времени
        Task oldTask = tasks.get(id);
        clearTimeSlots(oldTask);
        if (isTimeFree(task)) {
            prioritizedTasks.remove(oldTask);
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            fillTimeSlots(oldTask);
            throw new IllegalArgumentException("Не более одной задачи за раз");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        int id = epic.getId();
        if (id == 0) {
            throw new IllegalArgumentException("Можно обновить только добавленный эпик с присвоенным id");
        }
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпика с id " + id + " не существует");
        }
        epics.put(id, epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (id == 0) {
            throw new IllegalArgumentException("Можно обновить только добавленную подзадачу с присвоенным id");
        }
        if (!subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадачи с id " + id + " не существует");
        }
        Subtask oldSubtask = subtasks.get(id);
        clearTimeSlots(oldSubtask);
        if (isTimeFree(subtask)) {
            clearTimeSlots(oldSubtask);
            prioritizedTasks.remove(oldSubtask);
            subtasks.put(subtask.getId(), subtask);
            //обновление эпика
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            epic.setStatusBySubtask(subtask, subtasks);
            if (subtask.getStartTime().isPresent() || subtask.getDuration().isPresent()) {
                epic.setStartTime(subtasks);
                epic.setEndTime(subtasks);
                epic.setDuration(subtasks);
            }
            prioritizedTasks.add(subtask);
        } else {
            fillTimeSlots(oldSubtask);
        }
    }

    @Override
    public void removeTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задачи с id " + id + " не существует");
        }
        clearTimeSlots(tasks.get(id));
        prioritizedTasks.remove(tasks.get(id));
        if (historyManager.getHistory().contains(tasks.get(id))) {
            historyManager.remove(id);
        }
        tasks.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпика с id " + id + " не существует");
        }
        for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
            clearTimeSlots(subtasks.get(subtaskId));
            prioritizedTasks.remove(subtasks.get(subtaskId));
            historyManager.remove(subtaskId);
            subtasks.remove(subtaskId);
        }
        if (historyManager.getHistory().contains(epics.get(id))) {
            historyManager.remove(id);
        }
        epics.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадачи с id " + id + " не существует");
        }
        int epicId = getSubtask(id).getEpicId();
        Epic epic = epics.get(epicId);
        clearTimeSlots(subtasks.get(id));
        prioritizedTasks.remove(subtasks.get(id));
        if (historyManager.getHistory().contains(subtasks.get(id))) {
            historyManager.remove(id);
        }
        subtasks.remove(id);
        //обновление эпика
        epic.removeSubtaskId(id);
        epic.setStatus(subtasks);
        epic.setStartTime(subtasks);
        epic.setEndTime(subtasks);
        epic.setDuration(subtasks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return nextId == that.nextId
                && tasks.equals(that.tasks)
                && epics.equals(that.epics)
                && subtasks.equals(that.subtasks)
                && historyManager.equals(that.historyManager)
                && prioritizedTasks.equals(that.prioritizedTasks)
                && taskTimeBusiness.equals(that.taskTimeBusiness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextId, tasks, epics, subtasks, historyManager, prioritizedTasks, taskTimeBusiness);
    }
}