import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Scanner;

public class WeatherService {

    private static final String API_KEY = "8b73c6ca-23b6-40ff-9c8a-a3d4170b967b";
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) throws IOException, InterruptedException {

        double lat = 52.37125;
        double lon = 4.89388;

        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();

            System.out.println("Полный ответ сервиса:\n" + responseBody);

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

            JsonElement tempElement = jsonObject.getAsJsonObject("fact").get("temp");
            if (tempElement.isJsonPrimitive()) {
                double temperature = tempElement.getAsDouble();
                System.out.println("\nТекущая температура: " + temperature + "°C");
            } else {
                System.out.println("Не удалось получить температуру из ответа.");
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите количество дней для анализа:");
            int limit = scanner.nextInt();

            double totalTemperature = 0;
            int count = 0;
            JsonElement forecasts = jsonObject.getAsJsonArray("forecasts");
            if (forecasts.isJsonArray()) {
                for (int i = 0; i < limit && i < forecasts.getAsJsonArray().size(); i++) {
                    JsonElement forecast = forecasts.getAsJsonArray().get(i);
                    JsonObject parts = forecast.getAsJsonObject().get("parts").getAsJsonObject();

                    double dayTemperature = 0;
                    for (String partName : new String[]{"morning", "day", "evening", "night"}) {
                        JsonElement tempElementPart = parts.getAsJsonObject(partName).get("temp_avg");
                        if (tempElementPart.isJsonPrimitive()) {
                            dayTemperature += tempElementPart.getAsDouble();
                        } else {
                            System.out.println("Не удалось получить температуру для " + partName + ".");
                        }
                    }

                    if (dayTemperature > 0) {
                        totalTemperature += dayTemperature / 4;
                        count++;
                    } else {
                        System.out.println("Не удалось получить данные о температуре для этого дня.");
                    }
                }

                if (count > 0) {
                    double averageTemperature = totalTemperature / count;
                    System.out.println("\nСредняя температура за " + count + " дней: " + averageTemperature + "°C");
                } else {
                    System.out.println("Не удалось получить данные о температуре за заданный период.");
                }

            } else {
                System.out.println("Ответ не содержит прогнозов.");
            }

        }
    }
}