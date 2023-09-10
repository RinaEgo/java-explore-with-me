package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.util.DateTimeFormatterUtil;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

@UtilityClass
public class RequestMapper {
    public RequestDto toRequestDto(Request request) {
        return RequestDto
                .builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(DateTimeFormatterUtil.dateTimeToString(request.getCreateDate()))
                .status(request.getStatus().toString())
                .build();
    }
}
