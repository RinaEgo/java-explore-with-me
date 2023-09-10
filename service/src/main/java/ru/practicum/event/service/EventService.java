package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.State;
import ru.practicum.request.dto.RequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {

    EventDto create(Long userId, NewEventDto newEventDto);

    EventDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventDto updateAdmin(Long eventId, UpdateEventDto updateEventDto);

    List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     Boolean onlyAvailable, String sort,
                                     HttpServletRequest httpRequest, int from, int size);

    List<EventDto> getAllAdmin(List<Long> users,
                               Collection<State> states,
                               List<Long> categories,
                               LocalDateTime rangeStart,
                               LocalDateTime rangeEnd,
                               int from, int size);

    List<EventShortDto> getAllByUser(Long userId, int from, int size);

    EventDto getById(Long eventId, Long userId);

    EventDto getByIdPublic(Long eventId, HttpServletRequest httpRequest);

    List<RequestDto> getAllRequests(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
