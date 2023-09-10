package ru.practicum.request.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RequestDto {

    private Long id;
    private Long event;
    private Long requester;
    private String created;
    private String status;
}
