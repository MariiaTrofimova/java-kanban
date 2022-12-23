package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    public static final int HISTORY_SIZE = 10;

    private static final List<Task> taskHistory = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (taskHistory.size() == HISTORY_SIZE) {
            taskHistory.remove(0);
        }
        taskHistory.add(task);
    }

    @Override
    public  List<Task> getHistory() {
        if (taskHistory.isEmpty()) {
            System.out.println("Вы еще не смотрели ни одной задачи, а надо бы");
        }
        return taskHistory;
    }

}
