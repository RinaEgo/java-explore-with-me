package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NewCompilationDto {

    private Long id;
    private List<Long> events;
    private boolean pinned;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
