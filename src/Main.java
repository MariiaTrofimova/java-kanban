import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import service.InMemoryTaskManager;
import service.Managers;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        taskManager.clearTasks();
        taskManager.clearEpics();

        //Создайте несколько задач разного типа
        System.out.println("-".repeat(20)
                + "Создайте несколько задач разного типа"
                + "-".repeat(20));

        taskManager.createTask(new Task("Дожить до Нового года", "Как-нибудь постараемся"));
        taskManager.createTask(new Task("Встретить Новый год", "Как-нибудь тоже справимся"));

        Epic epic = new Epic("Покормить кота",
                "Если не покормить — будет кусь и ночной тыгыдык");
        taskManager.createEpic(epic);
        taskManager.createSubtask((new Subtask("Купить корм", "Pro Plan 1,5 кг")), epic.getId());
        taskManager.createSubtask((new Subtask("Положить корм", "Две ложки")), epic.getId());

        epic = new Epic("Подготовиться к приходу гостей", "Дом должен быть чистым и хорошо пахнуть");

        taskManager.createEpic(epic);
        taskManager.createSubtask((new Subtask("Убрать лоток кота",
                "Если опилки закончились, свежие взять на балконе за лыжами")), epic.getId());
        printLists(taskManager);
        printHistory();

        //Вызовите разные методы интерфейса TaskManager и напечатайте историю просмотров после каждого вызова.
        System.out.println("-".repeat(20)
                + "Вызовите разные методы интерфейса TaskManager и напечатайте историю просмотров после каждого вызова."
                + "-".repeat(20));

        taskManager.updateTask(new Task(0, "Дожить до Нового года", "Как-нибудь постараемся",
                Status.IN_PROGRESS));
        printHistory();

        taskManager.updateSubtask(new Subtask(3, "Купить корм", "Pro Plan 1,5 кг", Status.DONE, 2));
        printHistory();

        taskManager.getEpic(5);
        printHistory();

        taskManager.getSubtask(3);
        printHistory();

        taskManager.getTask(0);

        taskManager.removeEpic(2);
        printHistory();
    }

    public static void printHistory() {
        System.out.println("История просмотров:");
        if (!Managers.getDefaultHistory().getHistory().isEmpty()) {
            for (Task task : Managers.getDefaultHistory().getHistory()) {
                System.out.println(task);
            }
        }
    }

    public static void printLists(InMemoryTaskManager taskManager) {
        System.out.println("Список задач:");
        if (!taskManager.getTasksList().isEmpty()) {
            for (Task task : taskManager.getTasksList()) {
                System.out.println(task);
            }
        }
        System.out.println("Список эпиков:");
        if (!taskManager.getEpicsList().isEmpty()) {
            for (Task epic : taskManager.getEpicsList()) {
                System.out.println(epic);
            }
        }
        System.out.println("Список подзадач:");
        if (!taskManager.getSubtasksList().isEmpty()) {
            for (Task subtask : taskManager.getSubtasksList()) {
                System.out.println(subtask);
            }
        }
    }
}
