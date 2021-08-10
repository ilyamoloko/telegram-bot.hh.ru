package ru.betry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class App {

    static class Job {
        String id;
        String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class HH {
        List<Job> items;

        public List<Job> getItems() {
            return items;
        }

        public void setItems(List<Job> items) {
            this.items = items;
        }

        HH(){}
    }

    public static void main(String[] args) {

        TelegramBot bot = new TelegramBot("1931046020:AAGq9gvLlYewYBYLqGJq-HUbLER1vsDh3aM");
        bot.setUpdatesListener(element -> {
            System.out.println(element);
            element.stream()
                    .filter(it -> it.message() != null)
                    .filter(it -> it.message().text() != null)
                    .forEach(it -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hh.ru/vacancies?text=" + URLEncoder.encode(it.message().text(), StandardCharsets.UTF_8) + "&area=2"))
                        .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = response.body();
                    System.out.println(body);
                    HH hh = mapper.readValue(body, HH.class);
                    hh.items.stream().limit(5).forEach(job -> {
                        String text = "Вакансия: " + job.name + "\nСсылка: http://hh.ru/vacancy/" + job.id;
                        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

                        String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
                        bot.execute(new SendMessage(it.message().chat().id(), utf8EncodedString));
                        System.out.println(job.id + " " + job.name);
                    });
                    response.body();
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }

}
