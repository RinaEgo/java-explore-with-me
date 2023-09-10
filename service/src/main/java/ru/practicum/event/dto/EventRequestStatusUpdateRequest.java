package ru.practicum.event.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventRequestStatusUpdateRequest {

    private Long[] requestIds;
    private String status;
}
