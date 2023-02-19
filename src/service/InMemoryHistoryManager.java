package service;

import model.Task;

import java.util.List;
import java.util.Objects;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList taskHistory = new CustomLinkedList();

    @Override
    public void add(Task task) {
        int id = task.getId();

        if (taskHistory.getNodes().containsKey(id)) {
            taskHistory.removeNode(taskHistory.getNodes().get(id));
        }
        taskHistory.linkLast(task);
    }

    @Override
    public void remove(int id) {
        if (taskHistory.getNodes().containsKey(id)) {
            taskHistory.removeNode(taskHistory.getNodes().get(id));
        } else {
            throw new IllegalArgumentException("Задачи с id " + id + " нет в истории просмотров");
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryHistoryManager that = (InMemoryHistoryManager) o;
        return taskHistory.getTasks().equals(that.taskHistory.getTasks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskHistory);
    }
}
