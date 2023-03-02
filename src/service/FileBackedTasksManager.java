package service;

import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager {
    String path;

    public FileBackedTasksManager(String path) {
        super();
        this.path = path;
        //this.file = new File(path);
    }

    public static void main(String[] args) {
        String path = "src" + File.separator + "resources" + File.separator + "log.csv";
        //File file = new File(path);
        FileBackedTasksManager manager = new FileBackedTasksManager(path);

        //Заведите несколько разных задач, эпиков и подзадач.
        manager.addTask(new Task("Сдать ТЗ", "Постарайся"));
        Task updateTask = manager.getTask(manager.nextId - 1);
        updateTask.setStartTime(Optional.of((LocalDateTime.now().withSecond(0).withNano(0))));
        updateTask.setDuration(Optional.of((long) 2 * 24 * 60));
        manager.updateTask(updateTask);


        Epic epic = new Epic("Покормить кота",
                "Два раза в день");
        manager.addEpic(epic);
        manager.addSubtask((new Subtask("Помыть миски", "Без мыла")), epic.getId());
        manager.addSubtask((new Subtask("Положить корм", "Две ложки")), epic.getId());
        manager.addSubtask((new Subtask("Налить воды", "Без минералов")), epic.getId());
        //проверка обновления времени эпика
        Subtask updateSubtask = manager.getSubtask(manager.nextId - 1);
        updateSubtask.setStartTime(Optional.of((LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))));
        updateSubtask.setDuration(Optional.of((long) 23));
        manager.updateSubtask(updateSubtask);

        epic = new Epic("Функционалка", "Освоить");
        manager.addEpic(epic);

        //Дополнительная проверка + к ТЗ, что везде работает обработка статусов.
        manager.subtasks.get(3).setStatus(Status.DONE);
        manager.updateSubtask(manager.subtasks.get(3));

        //Запросите некоторые из них, чтобы заполнилась история просмотра.
        manager.getEpic(6);
        manager.getTask(1);
        manager.getSubtask(4);
        manager.getSubtask(5);
        System.out.println("-".repeat(10) + "Содержимое менеджера" + "-".repeat(10));
        System.out.println("id,type,name,status,description,epic,duration,startTime,endTime");
        printManager(manager);
        System.out.println("-".repeat(48));

        //Создайте новый FileBackedTasksManager менеджер из этого же файла.
        //Проверьте, что история просмотра восстановилась верно
        // и все задачи, эпики, подзадачи, которые были в старом, есть в новом менеджере.
        FileBackedTasksManager managerFromFile = loadFromFile(path);
        System.out.println("-".repeat(10) + "Содержимое менеджера из файла" + "-".repeat(10));
        System.out.println("id,type,name,status,description,epic,duration,startTime,endTime");
        printManager(managerFromFile);
        System.out.println("-".repeat(48));

        //Проверка идентификатора nextId
        managerFromFile.addTask(new Task("Проверить nextId", "Должно быть max + 1"));
        managerFromFile.getTask(managerFromFile.nextId - 1);
        managerFromFile.addSubtask((new Subtask("Тестирование", "Тестировать как леди Баг и супер код")), 6);
        managerFromFile.getSubtask(managerFromFile.nextId - 1);

        updateSubtask = managerFromFile.getSubtask(managerFromFile.nextId - 1);
        updateSubtask.setStartTime(Optional.of((LocalDateTime.now().plusDays(7).withSecond(0).withNano(0))));
        managerFromFile.updateSubtask(updateSubtask);

        System.out.println("-".repeat(2) + "Проверка идентификатора в менеджере из файла" + "-".repeat(2));
        System.out.println("id,type,name,status,description,epic,duration,startTime,endTime");
        printManager(managerFromFile);

        //Проверим getPrioritizedTasks()
        System.out.println("-".repeat(15) + "Список по приоритету" + "-".repeat(15));
        for (Task prioritizedTask : manager.getPrioritizedTasks()) {
            System.out.println(prioritizedTask);
        }
        System.out.println("-".repeat(10) + "Список по приоритету из файла" + "-".repeat(10));
        for (Task prioritizedTask : managerFromFile.getPrioritizedTasks()) {
            System.out.println(prioritizedTask);
        }
    }

    public void save() throws ManagerSaveException {
        try (FileWriter fileWriter = new FileWriter(path, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic,duration,startTime,endTime\n");
            if (!tasks.isEmpty()) {
                for (Task task : getTasks()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!epics.isEmpty()) {
                for (Task task : getEpics()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!subtasks.isEmpty()) {
                for (Task task : getSubtasks()) {
                    fileWriter.write(toString(task) + "\n");
                }
            }
            if (!historyManager.getHistory().isEmpty()) {
                fileWriter.write("\n" + historyToString(historyManager));
            } else fileWriter.write("\n ");
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранениив в файл");
        }
    }

    public static String toString(Task task) {
        TaskType taskType = TaskType.valueOf(task.getClass().getSimpleName().toUpperCase());
        String epicId = " ";
        String startTime = " ";
        String duration = " ";
        String endTime = " ";
        if (taskType == TaskType.SUBTASK) epicId = String.valueOf(((Subtask) task).getEpicId());
        if (task.getStartTime().isPresent()) {
            startTime = task.getStartTime().get().format(task.getFormatter());
            if (taskType == TaskType.EPIC) {
                endTime = task.getEndTime().get().format(task.getFormatter());
            }
        }
        if (task.getDuration().isPresent()) {
            duration = String.valueOf(task.getDuration().get());
        }

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), taskType, task.getTitle(), task.getStatus(), task.getDescription(), epicId,
                duration, startTime, endTime);
    }

    public Task fromString(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Чтение пустой строки невозможно");
        } else if (value.split(",").length != 9) {
            throw new IllegalArgumentException("Неверное число элементов в строке");
        }
        String[] split = value.split(",");
        Status status = Status.valueOf(split[3]);
        Optional<LocalDateTime> startTime;
        Optional<LocalDateTime> endTime;
        Optional<Long> duration;
        if (split[6].isBlank()) {
            duration = Optional.empty();
        } else {
            duration = Optional.of(Long.parseLong(split[6]));
        }
        if (split[7].isBlank()) {
            startTime = Optional.empty();
        } else {
            startTime = Optional.of(LocalDateTime.parse(split[7], Task.formatter));
        }
        if (!split[8].isBlank() && split[1].equals("EPIC")) {
            endTime = Optional.of(LocalDateTime.parse(split[8], Task.formatter));
        } else {
            endTime = Optional.empty();
        }
        switch (split[1]) {
            case "TASK":
                return new Task(Integer.parseInt(split[0]), split[2], split[4], status,
                        duration, startTime);
            case "EPIC":
                Epic epic = new Epic(Integer.parseInt(split[0]), split[2], split[4]);
                epic.setStatus(status);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
                epic.setEndTime(endTime);
                return epic;
            case "SUBTASK":
                return new Subtask(Integer.parseInt(split[0]), split[2], split[4], status, Integer.parseInt(split[5]),
                        duration, startTime);
        }
        return null;
    }

    public static String historyToString(HistoryManager manager) {
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

    public static List<Integer> historyFromString(String value) {
        //для восстановления менеджера истории из CSV
        if (value.isBlank()) {
            return Collections.emptyList();
        }
        List<Integer> history = new ArrayList<>();
        String[] split = value.split(",");
        if (split.length == 0) {
            return Collections.emptyList();
        }
        for (String s : split) {
            history.add(Integer.parseInt(s));
        }
        return history;
    }

    public static FileBackedTasksManager loadFromFile(String path) {
        FileBackedTasksManager managerFromFile = new FileBackedTasksManager(path);
        try {
            int maxId = 0;
            String content = Files.readString(Path.of(path));
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length - 2; i++) {
                Task task = managerFromFile.fromString(lines[i]);
                TaskType taskType = TaskType.valueOf(task.getClass().getSimpleName().toUpperCase());
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
                switch (taskType) {
                    case EPIC -> managerFromFile.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        managerFromFile.subtasks.put(task.getId(), (Subtask) task);
                        int epicId = ((Subtask) task).getEpicId();
                        Epic epic = managerFromFile.epics.get(epicId);
                        epic.addSubtask(task.getId());
                        managerFromFile.prioritizedTasks.add(task);
                        managerFromFile.fillTimeSlots(task);
                    }
                    case TASK -> {
                        managerFromFile.tasks.put(task.getId(), task);
                        managerFromFile.prioritizedTasks.add(task);
                        managerFromFile.fillTimeSlots(task);
                    }
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
            managerFromFile.nextId = maxId + 1;
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

    private static void printManager(FileBackedTasksManager manager) {
        for (Task task : manager.getTasks()) {
            System.out.println(toString(task));
        }
        for (Task task : manager.getEpics()) {
            System.out.println(toString(task));
        }
        for (Task task : manager.getSubtasks()) {
            System.out.println(toString(task));
        }
        System.out.println("\n" + historyToString(manager.historyManager));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileBackedTasksManager that = (FileBackedTasksManager) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}