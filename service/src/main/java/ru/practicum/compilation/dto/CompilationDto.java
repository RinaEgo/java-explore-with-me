package ru.practicum.compilation.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompilationDto {

    private Long id;
    private List<EventShortDto> events;
    private boolean pinned;
    private String title;
}
