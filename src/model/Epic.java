package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(int id, String title, String description) {
        super(id, title, description);
        subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(Integer subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            System.out.println("Такой подзадачи не существует");
            return;
        }
        subtaskIds.remove(subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{"
                + ", id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", status='" + status + '\''
                + ", subtaskIds='" + subtaskIds.toString() + '\''
                + '}';
    }
}
