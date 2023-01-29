package service;

import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTasksManager(File file) {
        super();
        this.file = file;
    }

    public static void main(String[] args) {
        String path = "src" + File.separator + "resources" + File.separator + "log.csv";
        File file = new File(path);
        FileBackedTasksManager manager = new FileBackedTasksManager(file);

        //Заведите несколько разных задач, эпиков и подзадач.
        manager.addTask(new Task("Сдать ТЗ", "Постарайся"));
        Epic epic = new Epic("Покормить кота",
                "Два раза в день");
        manager.addEpic(epic);
        manager.addSubtask((new Subtask("Помыть миски", "Без мыла")), epic.getId());
        manager.addSubtask((new Subtask("Положить корм", "Две ложки")), epic.getId());
        manager.addSubtask((new Subtask("Налить воды", "Без минералов")), epic.getId());
        epic = new Epic("Оклематься", "Лежи");
        manager.addEpic(epic);

        //Дополнительная проверка + к ТЗ, что везде работает обработка статусов.
        manager.subtasks.get(3).setStatus(Status.DONE);
        manager.updateSubtask(manager.subtasks.get(3));

        //Запросите некоторые из них, чтобы заполнилась история просмотра.
        manager.getEpic(5);
        manager.getTask(0);
        manager.getSubtask(3);
        manager.getSubtask(4);

        //Создайте новый FileBackedTasksManager менеджер из этого же файла.
        //Проверьте, что история просмотра восстановилась верно
        // и все задачи, эпики, подзадачи, которые были в старом, есть в новом менеджере.
        FileBackedTasksManager managerFromFile = loadFromFile(file);
        System.out.println("-".repeat(10) + "Содержимое менеджера из файла" + "-".repeat(10));
        System.out.println("id,type,name,status,description,epic");
        for (Task task : managerFromFile.getTasksList()) {
            System.out.println(toString(task));
        }
        for (Task task : managerFromFile.getEpicsList()) {
            System.out.println(toString(task));
        }
        for (Task task : managerFromFile.getSubtasksList()) {
            System.out.println(toString(task));
        }
        System.out.println("\n" + historyToString(managerFromFile.historyManager));
        System.out.println("-".repeat(48));
    }

    void save() throws ManagerSaveException {
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            if (!tasks.isEmpty()) {
                for (Task task : getTasksList()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!epics.isEmpty()) {
                for (Task task : getEpicsList()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!subtasks.isEmpty()) {
                for (Task task : getSubtasksList()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!historyManager.getHistory().isEmpty()) {
                fileWriter.write("\n" + historyToString(historyManager));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранениив в файл");
        }
    }

    static String toString(Task task) {
        TaskType taskType;
        String epicId = "";
        if (task instanceof Epic) {
            taskType = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            taskType = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else {
            taskType = TaskType.TASK;
        }
        return String.format("%s,%s,%s,%s,%s,%s",
                task.getId(), taskType, task.getTitle(), task.getStatus(), task.getDescription(), epicId);
    }

    Task fromString(String value) {
        String[] split = value.split(",");
        Status status = Status.valueOf(split[3]);
        switch (split[1]) {
            case "TASK":
                return new Task(Integer.parseInt(split[0]), split[2], split[4], status);
            case "EPIC":
                return new Epic(Integer.parseInt(split[0]), split[2], split[4]);
            case "SUBTASK":
                return new Subtask(Integer.parseInt(split[0]), split[2], split[4], status, Integer.parseInt(split[5]));
        }
        return null;
    }

    static String historyToString(HistoryManager manager) {
        //для сохранения менеджера истории в CSV
        List<Task> historyList = manager.getHistory();
        String[] historyArray = new String[historyList.size()];
        int i = 0;

        for (Task task : historyList) {
            historyArray[i] = String.valueOf(task.getId());
            i++;
        }
        return String.join(",", historyArray);
    }

    static List<Integer> historyFromString(String value) {
        //для восстановления менеджера истории из CSV
        List<Integer> history = new ArrayList<>();
        String[] split = value.split(",");
        for (String s : split) {
            history.add(Integer.parseInt(s));
        }
        return history;
    }

    static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager managerFromFile = new FileBackedTasksManager(file);
        try {
            String content = Files.readString(Path.of(String.valueOf(file)));
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length - 2; i++) {
                Task task = managerFromFile.fromString(lines[i]);
                if (task instanceof Epic) {
                    managerFromFile.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    managerFromFile.subtasks.put(task.getId(), (Subtask) task);
                    int epicId = ((Subtask) task).getEpicId();
                    managerFromFile.epics.get(epicId).addSubtask(task.getId());
                    managerFromFile.setEpicStatus((Subtask) task);
                } else {
                    managerFromFile.tasks.put(task.getId(), task);
                }
            }
            List<Integer> history = historyFromString(lines[lines.length - 1]);
            HistoryManager historyManager = managerFromFile.historyManager;
            for (Integer id : history) {
                if (managerFromFile.epics.containsKey(id)) {
                    historyManager.add(managerFromFile.epics.get(id));
                } else if (managerFromFile.subtasks.containsKey(id)) {
                    historyManager.add(managerFromFile.subtasks.get(id));
                } else if (managerFromFile.tasks.containsKey(id)) {
                    historyManager.add(managerFromFile.tasks.get(id));
                }
            }
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл.");
        }
        return managerFromFile;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask, int epicId) {
        super.addSubtask(subtask, epicId);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }
}