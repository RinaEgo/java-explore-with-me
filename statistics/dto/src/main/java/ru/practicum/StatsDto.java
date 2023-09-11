package ru.practicum;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsDto {

    private String app;
    private String uri;
    private Long hits;
}
