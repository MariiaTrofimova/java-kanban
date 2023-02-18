package test;

import service.InMemoryTaskManager;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    void setTaskManager() {
        taskManager = new InMemoryTaskManager();
    }
}