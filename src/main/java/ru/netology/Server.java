package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("The server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> HandleConnection(clientSocket));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    private void HandleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {

            // Чтение строки запроса
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ", 3);
            if (parts.length < 3) return;
            String method = parts[0];
            String fullPath = parts[1];

            // Разбираем путь и query-строку
            String path;
            String queryString = null;
            int queryIndex = fullPath.indexOf('?');
            if (queryIndex >= 0) {
                path = fullPath.substring(0, queryIndex);
                queryString = fullPath.substring(queryIndex + 1);
            } else {
                path = fullPath;
            }

            // Чтение заголовков
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while (!(headerLine = in.readLine()).isEmpty()) {
                int colonIndex = headerLine.indexOf(':');
                if (colonIndex > 0) {
                    String name = headerLine.substring(0, colonIndex).trim();
                    String value = headerLine.substring(colonIndex + 1).trim();
                    headers.put(name, value);
                }
            }

            // Чтение тела запроса, если есть Content-Length
            String body = "";
            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr != null) {
                int contentLength = Integer.parseInt(contentLengthStr);
                char[] bodyChars = new char[contentLength];
                int read = in.read(bodyChars, 0, contentLength);
                if (read > 0) {
                    body = new String(bodyChars, 0, read);
                }
            }

            // Формируем объект запроса
            Request request = new Request(method, path, queryString, headers, body);

            // Ищем подходящий обработчик
            Handler handler = getHandler(method, path);
            String responseBody;
            String statusLine;
            if (handler != null) {
                responseBody = handler.handle(request);
                statusLine = "HTTP/1.1 200 OK";
            } else {
                responseBody = "Not Found";
                statusLine = "HTTP/1.1 404 Not Found";
            }

            // Отправляем ответ
            String httpResponse = statusLine + "\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody;
            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private Handler getHandler(String method, String path) {
        ConcurrentHashMap<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers != null) {
            return methodHandlers.get(path);
        }
        return null;
    }

}