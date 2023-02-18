package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Task implements Comparable<Task> {
    protected int id;
    protected String title;
    protected String description;
    protected Status status;

    //продолжительность задачи в минутах
    protected Optional<Long> duration = Optional.empty();

    //дата, когда предполагается приступить к выполнению задачи.
    protected Optional<LocalDateTime> startTime = Optional.empty();

    public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public Task(int id, String title, String description, Status status, Optional<Long> duration, Optional<LocalDateTime> startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        status = Status.NEW;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        status = Status.NEW;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Optional<LocalDateTime> getEndTime() {
        //проверить, что время начала определено?
        Optional<LocalDateTime> endTime = Optional.empty();
        if (startTime.isPresent()) {
            endTime = Optional.of(startTime.get().plusMinutes(duration.orElse(0L)));
        }
        return endTime;
    }

    public Optional<Long> getDuration() {
        return duration;
    }

    public void setDuration(Optional<Long> duration) {
        this.duration = duration;
    }

    public Optional<LocalDateTime> getStartTime() {
        return startTime;
    }

    public void setStartTime(Optional<LocalDateTime> startTime) {
        this.startTime = startTime;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    @Override
    public String toString() {
        StringBuilder taskToString = new StringBuilder("Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=");
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
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description) && status == task.status && Objects.equals(duration, task.duration) && Objects.equals(startTime, task.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, duration, startTime);
    }

    @Override
    public int compareTo(Task task) {
        return this.getId() - task.getId();
    }
}