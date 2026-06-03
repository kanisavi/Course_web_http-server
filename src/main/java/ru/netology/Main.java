package ru.netology;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(8087);
        server.start();
    }
}
