package service;

import model.Task;
import model.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    public static final int HISTORY_SIZE = 10;

    private static final CustomLinkedList taskHistory = new CustomLinkedList();

    @Override
    public void add(Task task) {
        int id = task.getId();
        if (taskHistory.size() == HISTORY_SIZE) {
            taskHistory.removeNode(taskHistory.head);
        }
        if (taskHistory.nodeMap.containsKey(id)) {
            taskHistory.removeNode(taskHistory.nodeMap.get(id));
        }
        taskHistory.linkLast(task);
    }

    @Override
    public void remove(int id) {
        if (taskHistory.nodeMap.containsKey(id)) {
            taskHistory.removeNode(taskHistory.nodeMap.get(id));
        } else {
            System.out.println("Задачи с id " + id + " нет в истории просмотров");
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }

    private static final class CustomLinkedList {
        private static final Map<Integer, Node> nodeMap = new HashMap<>();
        Node head = null;
        Node tail = null;
        //private int size = 0;

        public void linkLast(Task task) {
            //добавляет задачу в конец списка
            Node oldTail = tail;
            Node newNode = new Node(oldTail, task, null);
            tail = newNode;

            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.next = newNode;
            }

            nodeMap.put(task.getId(), newNode);
            //size++;
        }

        public ArrayList<Task> getTasks() {
            ArrayList<Task> tasks = new ArrayList<>();
            if (nodeMap.isEmpty()) {
                System.out.println("История просмотров пуста // и тут тоже");
            } else if (nodeMap.size() == 1) {
                tasks.add(tail.task);
            } else {
                Node node = head;
                while (node != tail) {
                    tasks.add(node.task);
                    node = node.next;
                }
                tasks.add(tail.task);
            }
            return tasks;
        }

        public void removeNode(Node node) {

            if (this.head == null) {
                System.out.println("История просмотров пуста");
            }

            if (node == head) {
                head = head.next;
            } else if (node == tail) {
                tail = tail.prev;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }

            nodeMap.remove(node.task.getId());
            //size--;
        }

        public int size() {
            return nodeMap.size();
        }
    }

}
