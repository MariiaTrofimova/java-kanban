package service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class Managers {
    public static TaskManager getDefault() throws IOException, InterruptedException {
        //return new InMemoryTaskManager();
        String url = "http://localhost:8078";
        return new HttpTaskManager(url);
    }

    public static FileBackedTasksManager getFileManager(String path) {
        return new FileBackedTasksManager(path);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        Type dateTimeType = new TypeToken<Optional<LocalDateTime>>() {
        }.getType();
        Type durationType = new TypeToken<Optional<Long>>() {
        }.getType();
/*        Type tasksListType = new TypeToken<List<Task>>() {
        }.getType();
        Type taskType = new TypeToken<Task>() {
        }.getType();
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Type epicType = new TypeToken<Epic>() {
        }.getType();*/

        return new GsonBuilder()
                //.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(dateTimeType, new OptionalLocalDateTimeAdapter())
                .registerTypeAdapter(durationType, new OptionalDurationAdapter())
                //.registerTypeAdapter(tasksListType, new TasksAdapter())
                //.serializeNulls()
                .create();
    }

/*    static class TasksAdapter extends TypeAdapter<Task> {

        @Override
        public void write(JsonWriter jsonWriter, Task task) throws IOException {
            if (task.getClass().equals(TaskType.EPIC)) {
                write(jsonWriter, (Epic) task);
            } else if (task.getClass().equals(TaskType.SUBTASK)) {
                write(jsonWriter, (Subtask) task);
            } else if (task.getClass().equals(TaskType.TASK)) {
                write(jsonWriter, task);
            }
        }

        @Override
        public Task read(JsonReader jsonReader) throws IOException {
            JsonObject json = JsonParser.parseString(jsonReader.nextString()).getAsJsonObject();
            String itemType = json.get("type").getAsString();
            if (itemType.equals(TaskType.TASK.toString())) {
                return read(jsonReader);
            }

            *//*JsonElement jsonElement = JsonParser.parseString(response.body()) ;
            JsonArray jsonArray = jsonElement.getAsJsonArray() ;
            ArrayList<Task> requestedItems = new ArrayList<>();
            for (JsonElement element : isonArray) {
                String itemType = element.getAsJson0bject().get ("itemType") .getAsString();
                if (itemType. equals (ItemType.TASK.toString())) {
                    requestedItems.add (gson. fromJson (element, taskType));
                } else if (itemType. equals(ItemType.SUBTASK.toString())){
                    requestedItems.add (gson.fromJson(element, subtaskType));
                }
                else if (itemType.equals (ItemType.EPIC. toString())) {
                    requestedItems.add*//*
            //return null;
        }
    }*/

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            jsonWriter.value(localDateTime.format(formatter));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), formatter);
        }
    }

    public static class OptionalLocalDateTimeAdapter extends TypeAdapter<Optional<LocalDateTime>> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        @Override
        public void write(JsonWriter jsonWriter, Optional<LocalDateTime> localDateTimeOptional) throws IOException {
            if (localDateTimeOptional.isEmpty()) {
                jsonWriter.value("");
                return;
            }
            LocalDateTime localDateTime = localDateTimeOptional.get();
            jsonWriter.value(localDateTime.format(formatter));
        }

        @Override
        public Optional<LocalDateTime> read(JsonReader jsonReader) throws IOException {
            String localDateTimeString = jsonReader.nextString();
            if (localDateTimeString.isBlank()) {
                return Optional.empty();
            }
            LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString, formatter);
            return Optional.of(localDateTime);
        }
    }

    public static class OptionalDurationAdapter extends TypeAdapter<Optional<Long>> {
        //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        @Override
        public void write(JsonWriter jsonWriter, Optional<Long> durationOptional) throws IOException {
            if (durationOptional.isEmpty()) {
                jsonWriter.value("");
                return;
            }
            Long duration = durationOptional.get();
            jsonWriter.value(duration);
        }

        @Override
        public Optional<Long> read(JsonReader jsonReader) throws IOException {
            String durationString = jsonReader.nextString();
            if (durationString.isBlank()) {
                return Optional.empty();
            }
            Long duration = Long.parseLong(durationString);
            return Optional.of(duration);
        }
    }

}
