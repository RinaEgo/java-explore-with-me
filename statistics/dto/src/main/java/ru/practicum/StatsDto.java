package ru.practicum;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDto {

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @NotBlank
    private long hits;
}
