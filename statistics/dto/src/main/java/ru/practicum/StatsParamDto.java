package ru.practicum;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StatsParamDto {
    private String start;
    private String end;
    private String[] uris;
    private boolean unique;
}
