package ru.netology;

@FunctionalInterface
public interface Handler {
    String handle(Request request);
}

