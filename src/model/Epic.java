package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds;

    private Optional<LocalDateTime> endTime = Optional.empty();

    public Epic(int id, String title, String description) {
        super(id, title, description);
        subtaskIds = new ArrayList<>();
        status = Status.NEW;
    }

    public Epic(String title, String description) {
        super(title, description);
        subtaskIds = new ArrayList<>();
        status = Status.NEW;
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
    public Optional<LocalDateTime> getEndTime() {
        return endTime;
    }

    public void setEndTime(Optional<LocalDateTime> endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        StringBuilder taskToString = new StringBuilder(
                "Epic{"
                        + "id=" + id
                        + ", title='" + title + '\''
                        + ", description='" + description + '\''
                        + ", status='" + status + '\''
                        + ", subtaskIds='" + subtaskIds.toString() + '\''
                        + ", duration="
        );
        if (duration.isPresent()) {
            taskToString.append(duration.get());
        } else {
            taskToString.append("не задано");
        }
        if (startTime.isPresent()) {
            taskToString.append(", startTime=" + startTime.get().format(formatter));
        } else {
            taskToString.append(", startTime= не задано");
        }
        taskToString.append("}");
        return taskToString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime);
    }
}