package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.addHandler("GET", "/messages", request -> {
            return "Returning all messages. Query params: " + request.getQueryParams();
        });

        server.addHandler("POST", "/messages", request -> {
            return "Message created successfully. Received body: " + request.getBody();
        });

        // удаление сообщения (по ID из query)
        server.addHandler("DELETE", "/messages", request -> {
            String id = request.getQueryParam("id");
            if (id == null) {
                return "Error: 'id' parameter is required for deletion.";
            }
            return "Message with ID " + id + " deleted.";
        });

        server.addHandler("GET", "/hello", request -> {
            String name = request.getQueryParam("name");
            return "Hello, " + (name != null ? name : "World") + "!";
        });

        server.start(8087);
    }
}
