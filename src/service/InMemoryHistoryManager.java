package service;

import model.Task;

import java.util.List;

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
            System.out.println("Задачи с id " + id + " нет в истории просмотров");
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }

}
