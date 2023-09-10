package ru.practicum.event.model;

public enum State {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static State from(String text) {
        for (State status : State.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null;
    }
}
