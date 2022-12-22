package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final List<Task> taskHistory = new ArrayList<>();
    //
    @Override
    public void add(Task task) {
        if (taskHistory.size() == 10) {
            taskHistory.remove(0);
        }
        taskHistory.add(task);
    }

    @Override
    public  List<Task> getHistory() {
        if (taskHistory.isEmpty()) {
            System.out.println("Вы еще не смотрели ни одной задачи, а надо бы");
            return null;
        }
        return taskHistory;
    }

}
