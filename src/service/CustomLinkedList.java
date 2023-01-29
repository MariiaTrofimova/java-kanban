package service;

import model.Node;
import model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CustomLinkedList {
    private final Map<Integer, Node> nodes = new HashMap<>();
    private Node head = null;
    private Node tail = null;

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
        nodes.put(task.getId(), newNode);
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!nodes.isEmpty()) {
            if (nodes.size() != 1) {
                Node node = head;
                while (node != tail) {
                    tasks.add(node.getTask());
                    node = node.getNext();
                }
            }
            tasks.add(tail.getTask());
        }
        return tasks;
    }

    public void removeNode(Node node) {

        if (this.head == null) {
            System.out.println("История пуста");
        }

        if (node == head) {
            head = head.getNext();
        } else if (node == tail) {
            tail = tail.getPrev();
        } else {
            node.getPrev().setNext(node.getNext());
            node.getNext().setPrev(node.getPrev());
        }

        nodes.remove(node.getTask().getId());
        //size--;
    }

    public int size() {
        return nodes.size();
    }
}
