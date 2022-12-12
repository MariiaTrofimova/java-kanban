package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String title, String description, Status status) {
        super(id, title, description, status);
    }

    public Subtask(int id, String title, String description) {
        super(id, title, description);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{"
                + "epicId=" + epicId
                + ", id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", status='" + status + '\''
                + '}';
    }
}
