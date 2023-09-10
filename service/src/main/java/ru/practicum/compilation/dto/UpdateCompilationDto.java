package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateCompilationDto {

    private Long id;
    private List<Long> events;
    private boolean pinned;

    @Size(min = 3, max = 120)
    private String title;
}
