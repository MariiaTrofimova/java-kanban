package model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds;

    private Optional<LocalDateTime> endTime;

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

    public void setStatusBySubtask(Subtask subtask, HashMap<Integer, Subtask> subtasks) {
        switch (subtask.getStatus()) {
            case IN_PROGRESS:
                status = Status.IN_PROGRESS;
                break;
            case NEW:
                if (status == Status.DONE) {
                    status = Status.IN_PROGRESS;
                }
                break;
            case DONE:
                if (status != Status.DONE) {
                    //стрим из сабтасков: если хоть один NEW / IN_PROGRESS --> IN_PROGRESS
                    if (subtaskIds.size() == 1) {
                        status = Status.DONE;
                    } else if (subtaskIds.stream()
                            .map(subtasks::get)
                            .map(Task::getStatus)
                            .anyMatch(status -> status != Status.DONE)) {
                        status = Status.IN_PROGRESS;
                    } else {
                        status = Status.DONE;
                    }
                    break;
                }
                break;
            default:
                System.out.println("Некорректный статус");
        }
    }

    public void setStatus(HashMap<Integer, Subtask> subtasks) {
        if (subtaskIds.isEmpty()) {
            status = Status.NEW;
            return;
        }
        List<Status> epicSubtasksStatus = subtaskIds.stream()
                .map(subtasks::get)
                .map(Task::getStatus)
                .collect(Collectors.toList());
        if (epicSubtasksStatus.stream()
                .allMatch(s -> s == Status.NEW)) {
            status = Status.NEW;
        } else if (epicSubtasksStatus.stream()
                .allMatch(s -> s == Status.DONE)) {
            status = Status.DONE;
        } else {
            status = Status.IN_PROGRESS;
        }
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return endTime;
    }

    public void setStartTime(HashMap<Integer, Subtask> subtasks) {
        startTime = subtaskIds.stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStartTime().isPresent())
                .map(subtask -> subtask.getStartTime().get())
                .min(LocalDateTime::compareTo);
    }

    public void setEndTime(HashMap<Integer, Subtask> subtasks) {
        endTime = subtaskIds.stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getEndTime().isPresent())
                .map(subtask -> subtask.getEndTime().get())
                .max(LocalDateTime::compareTo);
    }

    public void setDuration(HashMap<Integer, Subtask> subtasks) {
        long sum = subtaskIds.stream()
                .map(subtasks::get)
                //.filter(subtask -> subtask.getDuration().isPresent())
                .mapToLong(subtask -> subtask.getDuration().orElse(0L))
                .sum();
        duration = Optional.of(sum);
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