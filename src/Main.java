import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        taskManager.clearTasks();
        taskManager.clearEpics();

        int epicId;

        //Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        taskManager.createTask(new Task(taskManager.idGenerator++,
                "Дожить до Нового года", "Как-нибудь постараемся"));
        taskManager.createTask(new Task(taskManager.idGenerator++,
                "Встретить Новый год", "Как-нибудь тоже справимся"));

        epicId = taskManager.idGenerator++;
        taskManager.createEpic(new Epic(epicId, "Покормить кота",
                "Если не покормить — будет кусь и ночной тыгыдык"));
        taskManager.createSubtask((new Subtask(taskManager.idGenerator++, "Купить корм",
                "Pro Plan 1,5 кг")), epicId);
        taskManager.createSubtask((new Subtask(taskManager.idGenerator++, "Положить корм",
                "Две ложки")), epicId);

        epicId = taskManager.idGenerator++;
        taskManager.createEpic(new Epic(epicId, "Подготовиться к приходу гостей",
                "Дом должен быть чистым и хорошо пахнуть"));
        taskManager.createSubtask((new Subtask(taskManager.idGenerator++, "Убрать лоток кота",
                "Если опилки закончились, свежие взять на балконе за лыжами")), epicId);

        //Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
        System.out.println("-".repeat(20)
                + "Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей."
                + "-".repeat(20));
        printLists(taskManager);

        //Измените статусы созданных объектов,
        taskManager.updateTaskStatus(0, Status.IN_PROGRESS);
        taskManager.updateTaskStatus(1, Status.IN_PROGRESS);
        taskManager.updateSubtaskStatus(3, Status.DONE);
        taskManager.updateSubtaskStatus(4, Status.IN_PROGRESS);
        taskManager.updateSubtaskStatus(6, Status.DONE);

        // распечатайте.
        System.out.println("-".repeat(20) + "Измените статусы созданных объектов" + "-".repeat(20));
        printLists(taskManager);

        //Попробуйте удалить одну из задач и один из эпиков.
        taskManager.removeTask(1);
        taskManager.removeEpic(2);

        // распечатайте.
        System.out.println("-".repeat(20) + "Удалите одну из задач и один из эпиков"
                + "-".repeat(20));
        printLists(taskManager);

        //Для тестирования добавила удаление подзадачи.
        taskManager.removeSubtask(6);

        // Печать результата
        System.out.println("-".repeat(20) + "Для тестирования добавила удаление подзадачи"
                + "-".repeat(20));
        printLists(taskManager);

    }

    public static void printLists(TaskManager taskManager) {
        System.out.println("Список задач:");
        if (taskManager.getTasksList() != null) {
            for (Task task : taskManager.getTasksList()) {
                System.out.println(task);
            }
        }
        System.out.println("Список эпиков:");
        if (taskManager.getEpicsList() != null) {
            for (Task epic : taskManager.getEpicsList()) {
                System.out.println(epic);
            }
        }
        System.out.println("Список подзадач:");
        if (taskManager.getSubtasksList() != null) {
            for (Task subtask : taskManager.getSubtasksList()) {
                System.out.println(subtask);
            }
        }

    }

}
