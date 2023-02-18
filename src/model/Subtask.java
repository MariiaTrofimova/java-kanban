package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String title, String description, Status status, int epicId, Optional<Long> duration, Optional<LocalDateTime> startTime) {
        super(id, title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description) {
        super(title, description);
    }

    public Subtask(int id, String title, String description, int epicId) {
        super(id, title, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        StringBuilder taskToString = new StringBuilder(
                "Subtask{" +
                        "epicId=" + epicId +
                        ", id=" + id +
                        ", title='" + title + '\'' +
                        ", description='" + description + '\'' +
                        ", status=" + status +
                        ", duration="
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
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}
