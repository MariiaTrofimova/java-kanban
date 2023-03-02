package client;

import server.KVServer;
import service.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class KVTaskClient {

    private final String url;
    private final String API_TOKEN;
    private final HttpClient client;

    public KVTaskClient(String url) throws ManagerSaveException {
        //URL к серверу хранилища.
        this.url = url;
        URI uri = URI.create(url + "/register");
        client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Код состояния: " + response.statusCode());
            System.out.println("Ответ: " + response.body());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Код состояния: "
                        + response.statusCode() + " Ответ: " + response.body());
            }
            API_TOKEN = response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Ошибка выполнения запроса ресурса по url-адресу: " + url);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer server = new KVServer();
        server.start();
        KVTaskClient client = new KVTaskClient("http://localhost:8078");

        client.put("tasks", "[{\"id\":1,\"title\":\"Task\",\"description\":\"Task description\",\"status\":\"NEW\"}]");
        client.put("a", "b");
        System.out.println(client.load("tasks"));
        System.out.println(client.load("a"));
        client.put("a", "с");
        System.out.println(client.load("a"));
        server.stop();
    }

    public void put(String key, String json) {
        //сохраняет состояние менеджера задач через запрос POST /save/<ключ>?API_TOKEN=
        URI uri = URI.create(url + "/save/" + key + "?API_TOKEN=" + API_TOKEN);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Код состояния: "
                        + response.statusCode() + " Ответ: " + response.body());
            }
            System.out.println("Код ответа: " + response.statusCode());
            System.out.println("Тело ответа: " + response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса ресурса по url-адресу: '" + url + "', возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
            throw new ManagerSaveException("Ошибка выполнения запроса ресурса по url-адресу: " + url);
        }
    }

    public String load(String key) {
        //возвращает состояние менеджера задач через запрос GET /load/<ключ>?API_TOKEN=
        URI uri = URI.create(url + "/load/" + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Код состояния: "
                        + response.statusCode() + " Ответ: " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса ресурса по url-адресу: '" + url + "', возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KVTaskClient that = (KVTaskClient) o;
        return Objects.equals(url, that.url) && Objects.equals(API_TOKEN, that.API_TOKEN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, API_TOKEN);
    }
}