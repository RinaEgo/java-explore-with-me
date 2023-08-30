package ru.practicum.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    private String app;
    private String uri;
    private long hits;
}
