package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        return new GsonBuilder()
                .registerTypeAdapter(dateTimeType, new OptionalLocalDateTimeAdapter())
                .registerTypeAdapter(durationType, new OptionalDurationAdapter())
                .create();
    }

    public static class OptionalLocalDateTimeAdapter extends TypeAdapter<Optional<LocalDateTime>> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");

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
