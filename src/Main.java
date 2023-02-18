import model.Epic;
import model.Subtask;
import model.Task;

import service.HistoryManager;
import service.InMemoryTaskManager;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        taskManager.clearTasks();
        taskManager.clearEpics();

        //Создайте несколько задач разного типа
        System.out.println("-".repeat(20)
                + "Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач"
                + "-".repeat(20));

        taskManager.addTask(new Task("Закончить кашлять", "Лежи"));
        taskManager.addTask(new Task("Убрать квартиру", "До вторника"));

        Epic epic = new Epic("Покормить кота",
                "Два раза в день");
        taskManager.addEpic(epic);
        taskManager.addSubtask((new Subtask("Помыть миски", "Без мыла")), epic.getId());
        taskManager.addSubtask((new Subtask("Положить корм", "Две ложки")), epic.getId());
        taskManager.addSubtask((new Subtask("Налить воды", "Без минералов")), epic.getId());

        epic = new Epic("Поехать в отпуск", "И постараться до него дожить");
        taskManager.addEpic(epic);

        printLists(taskManager);

        //Запросите созданные задачи несколько раз в разном порядке;
        //после каждого запроса выведите историю и убедитесь, что в ней нет повторов;
        System.out.println("-".repeat(20)
                + "Запросите созданные задачи несколько раз в разном порядке."
                + "-".repeat(20));

        taskManager.getEpic(7);
        printHistory(taskManager.getHistoryManager());
        taskManager.getSubtask(4);
        printHistory(taskManager.getHistoryManager());
        taskManager.getSubtask(5);
        printHistory(taskManager.getHistoryManager());
        taskManager.getTask(2);
        printHistory(taskManager.getHistoryManager());
        taskManager.getEpic(7);
        printHistory(taskManager.getHistoryManager());
        taskManager.getTask(1);
        printHistory(taskManager.getHistoryManager());
        taskManager.getEpic(3);
        printHistory(taskManager.getHistoryManager());
        taskManager.getSubtask(6);
        printHistory(taskManager.getHistoryManager());
        taskManager.getSubtask(5);
        printHistory(taskManager.getHistoryManager());

        System.out.println("-".repeat(20)
                + "Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться"
                + "-".repeat(20));
        taskManager.removeTask(1);
        printHistory(taskManager.getHistoryManager());

        System.out.println("-".repeat(20)
                + "Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился "
                + "как сам эпик, так и все его подзадачи."
                + "-".repeat(20));
        taskManager.removeEpic(3);
        printHistory(taskManager.getHistoryManager());
    }

    public static void printHistory(HistoryManager historyManager) {
        System.out.println("История просмотров:");
        if (!historyManager.getHistory().isEmpty()) {
            for (Task task : historyManager.getHistory()) {
                System.out.println(task);
            }
        }
    }

    public static void printLists(InMemoryTaskManager taskManager) {
        System.out.println("Список задач:");
        if (!taskManager.getTasks().isEmpty()) {
            for (Task task : taskManager.getTasks()) {
                System.out.println(task);
            }
        }
        System.out.println("Список эпиков:");
        if (!taskManager.getEpics().isEmpty()) {
            for (Task epic : taskManager.getEpics()) {
                System.out.println(epic);
            }
        }
        System.out.println("Список подзадач:");
        if (!taskManager.getSubtasks().isEmpty()) {
            for (Task subtask : taskManager.getSubtasks()) {
                System.out.println(subtask);
            }
        }
    }
}
