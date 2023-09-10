package ru.practicum.user.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserShortDto {

    private Long id;
    private String name;
}
