package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;


public interface TaskManager {

    ArrayList<Task> getTasksList();

    ArrayList<Task> getEpicsList();

    ArrayList<Task> getSubtasksList();

    ArrayList<Subtask> getEpicSubtasks(int id);

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask, int epicId);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);
}
