package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.request.dto.RequestDto;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventRequestStatusUpdateResult {

    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;
}
