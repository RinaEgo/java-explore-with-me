package ru.practicum.event.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;

    @PatchMapping("/admin/events/{eventId}")
    public EventDto updateAdmin(@PathVariable Long eventId,
                                @RequestBody UpdateEventDto updateEventDto) {

        return eventService.updateAdmin(eventId, updateEventDto);
    }

    @GetMapping("/admin/events")
    public List<EventDto> getAllAdmin(@RequestParam(required = false) List<Long> users,
                                      @RequestParam(required = false) Collection<State> states,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                      @RequestParam(name = "from", defaultValue = "0") int from,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {

        return eventService.getAllAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/events")
    public List<EventShortDto> getAllPublic(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(name = "from", defaultValue = "0") int from,
                                            @RequestParam(name = "size", defaultValue = "10") int size,
                                            HttpServletRequest httpRequest) {

        return eventService.getAllPublic(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, httpRequest, from, size);
    }

    @GetMapping("/events/{eventId}")
    public EventDto getByIdPublic(@PathVariable Long eventId, HttpServletRequest httpRequest) {

        return eventService.getByIdPublic(eventId, httpRequest);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getAllByUser(@PathVariable Long userId,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return eventService.getAllByUser(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {

        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventDto getById(@PathVariable Long userId, @PathVariable Long eventId) {

        return eventService.getById(eventId, userId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDto update(@PathVariable Long userId, @PathVariable Long eventId,
                           @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {

        return eventService.update(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<RequestDto> getAllRequests(@PathVariable Long userId, @PathVariable Long eventId) {

        return eventService.getAllRequests(eventId, userId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        return eventService.updateStatusRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
