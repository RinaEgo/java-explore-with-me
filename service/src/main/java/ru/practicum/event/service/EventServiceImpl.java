package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatisticsClient;
import ru.practicum.StatsDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotAvailableException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.util.DateTimeFormatterUtil;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatisticsClient statisticsClient;

    @Value("${spring.application.name}")
    private String app;

    @Override
    public EventDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (DateTimeFormatterUtil.stringToDateTime(newEventDto.getEventDate())
                .isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Ошибка. Начало события должно быть не ранее " +
                    "чем через два часа от текущего момента.");
        }

        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + newEventDto.getCategory() + " не найдена.")));

        Event eventToSave = eventRepository.save(event);

        return EventMapper.toEventDto(eventToSave);
    }

    @Override
    public EventDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (updateEventUserRequest.getEventDate() != null) {
            if (DateTimeFormatterUtil.stringToDateTime(updateEventUserRequest.getEventDate())
                    .isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Ошибка. Начало события должно быть не ранее " +
                        "чем через два часа от текущего момента.");
            }
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new ValidationException("Ошибка. Пользователь не является инициатором события.");
        }
        if (event.getState() == State.PUBLISHED) {
            throw new NotAvailableException("Ошибка. Редактировать можно отмененные события или события, которые находятся" +
                    " на модерации");
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (!(updateEventUserRequest.getStateAction().equalsIgnoreCase("SEND_TO_REVIEW") ||
                    updateEventUserRequest.getStateAction().equalsIgnoreCase("CANCEL_REVIEW"))) {
                throw new ValidationException("Переданы некорректные данные в stateAction.");
            }
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + updateEventUserRequest.getCategory() + " не найдена.")));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(DateTimeFormatterUtil.stringToDateTime(updateEventUserRequest.getEventDate()));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != 0) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        if (updateEventUserRequest.getStateAction().equalsIgnoreCase("SEND_TO_REVIEW")) {
            event.setState(State.PENDING);
        }
        if (updateEventUserRequest.getStateAction().equalsIgnoreCase("CANCEL_REVIEW")) {
            event.setState(State.CANCELED);
        }

        final Event eventSaved = eventRepository.save(event);

        return mapToEventDto(List.of(eventSaved)).get(0);
    }

    @Override
    public EventDto updateAdmin(Long eventId, UpdateEventDto updateEventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (updateEventDto.getEventDate() != null) {
            if (DateTimeFormatterUtil.stringToDateTime(updateEventDto.getEventDate())
                    .isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Ошибка. Начало события должно быть не ранее " +
                        "чем через два часа от текущего момента.");
            }
        }
        if (updateEventDto.getStateAction().equals("PUBLISH_EVENT")) {
            if (!event.getState().equals(State.PENDING)) {
                throw new ValidationException("Ошибка. Событие не находится в состоянии ожидания публикации.");
            }
            event.setState(State.PUBLISHED);
        }
        if (updateEventDto.getStateAction().equals("REJECT_EVENT")) {
            if (event.getState().equals(State.PUBLISHED)) {
                throw new ValidationException("Ошибка. Опубликованное событие отклонить невозможно.");
            }
            event.setState(State.CANCELED);
        }

        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + updateEventDto.getCategory() + " не найдена.")));
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            event.setEventDate(DateTimeFormatterUtil.stringToDateTime(updateEventDto.getEventDate()));
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != 0) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getTitle() != null) {
            event.setTitle(updateEventDto.getTitle());
        }

        Optional.ofNullable(updateEventDto.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(updateEventDto.getRequestModeration()).ifPresent(event::setRequestModeration);

        final Event eventSaved = eventRepository.save(event);

        return mapToEventDto(List.of(eventSaved)).get(0);
    }

    @Override
    public List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, String sort,
                                            HttpServletRequest httpRequest, int from, int size) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Начальная дата запроа не может быть позже конечной.");
        }

        List<Event> events = eventRepository.findAllPublic(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, PageRequest.of(from / size, size));

        statisticsClient.addHit(httpRequest);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<EventShortDto> eventDtoList = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventsViews = getViews(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        eventDtoList.forEach(el -> {
            el.setViews(eventsViews.getOrDefault(el.getId(), 0L));
            el.setConfirmedRequests(confirmedRequests.getOrDefault(el.getId(), 0L));
        });

        if (sort != null) {
            switch (sort.toUpperCase()) {
                case "EVENT_DATE":
                    eventDtoList.sort(Comparator.comparing(EventShortDto::getEventDate));
                    break;
                case "VIEWS":
                    eventDtoList.sort(Comparator.comparing(EventShortDto::getViews));
                    break;
                default:
                    eventDtoList.sort(Comparator.comparing(EventShortDto::getId));
            }
        }

        return eventDtoList;
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Event> events = eventRepository.findAllByInitiatorId(userId,
                        PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id")));

        return mapToEventShortDto(events);
    }

    @Override
    public List<EventDto> getAllAdmin(List<Long> users, Collection<State> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        List<Event> events = eventRepository.findByAdmin(users, states, categories, rangeStart, rangeEnd,
                PageRequest.of(from / size, size));

        return mapToEventDto(events);
    }

    @Override
    public EventDto getById(Long eventId, Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);

        return mapToEventDto(List.of(event)).get(0);
    }

    @Override
    public EventDto getByIdPublic(Long eventId, HttpServletRequest httpRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Запрошенное событие не находится в статусе опубликовано");
        }

        statisticsClient.addHit(httpRequest, eventId);

        return mapToEventDto(List.of(event)).get(0);
    }

    @Override
    public List<RequestDto> getAllRequests(Long eventId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new BadRequestException("Ошибка. Пользователь не является инициатором события.");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new NotAvailableException("Ошибка. Пользователь не является инициатором события.");
        }
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new NotAvailableException("Этому запросу нельзя изменить статус");
        }

        final String status = eventRequestStatusUpdateRequest.getStatus();
        if (status.equals("CONFIRMED")) {
            if (event.getParticipantLimit() == event.getConfirmedRequests()) {
                throw new NotAvailableException("Достигнут лимит запросов на участие в событии");
            }

            for (int i = 0; i < eventRequestStatusUpdateRequest.getRequestIds().length; i++) {

                Request request = getRequestById(eventRequestStatusUpdateRequest.getRequestIds()[i]);

                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new NotAvailableException("Запрос не в статусе ожидания");
                }
                if (!request.getEvent().getId().equals(event.getId())) {
                    throw new NotAvailableException("Запрос не соответствует событию");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                requestRepository.save(request);
                long confirmedRequest = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
                event.setConfirmedRequests(confirmedRequest);
                eventRepository.save(event);
                if (event.getParticipantLimit() == event.getConfirmedRequests()) {
                    requestRepository.saveAll(requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING)
                            .stream()
                            .peek(e -> e.setStatus(RequestStatus.CANCELED))
                            .collect(Collectors.toList()));
                    break;
                }
            }
        } else if (status.equals("REJECTED")) {

            for (int i = 0; i < eventRequestStatusUpdateRequest.getRequestIds().length; i++) {

                final Request request = getRequestById(eventRequestStatusUpdateRequest.getRequestIds()[i]);

                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new NotAvailableException("Запрос не в статусе ожидания.");
                }
                if (!request.getEvent().getId().equals(event.getId())) {
                    throw new NotAvailableException("Запрос не соответствует событию.");
                }
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
            }
        } else {
            throw new BadRequestException("Статус некорректен.");
        }

        List<RequestDto> requestConfirmed = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
        List<RequestDto> rejectedConfirmed = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.REJECTED)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());

        return EventRequestStatusUpdateResult
                .builder()
                .confirmedRequests(requestConfirmed)
                .rejectedRequests(rejectedConfirmed)
                .build();
    }

    private Map<Long, Long> getViews(Collection<Long> eventsId) {
        List<String> uris = eventsId
                .stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        Optional<LocalDateTime> start = eventRepository.getStart(eventsId);

        Map<Long, Long> views = new HashMap<>();

        if (start.isPresent()) {
            List<StatsDto> response = statisticsClient
                    .getStats(start.get(), LocalDateTime.now(), uris, true);

            response.forEach(dto -> {
                String uri = dto.getUri();
                String[] split = uri.split("/");
                String id = split[2];
                Long eventId = Long.parseLong(id);
                views.put(eventId, dto.getHits());
            });
        } else {
            eventsId.forEach(el -> views.put(el, 0L));
        }

        return views;
    }

    private Map<Long, Long> getConfirmedRequests(Collection<Long> eventsId) {
        List<Request> confirmedRequests = requestRepository
                .findAllByStatusAndEventIdIn(RequestStatus.CONFIRMED, eventsId);

        return confirmedRequests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка на участие не найдена или недоступна"));
    }

    public List<EventDto> mapToEventDto(Collection<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<EventDto> eventDtoList = events.stream()
                .map(EventMapper::toEventDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventsViews = getViews(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        eventDtoList.forEach(el -> {
            el.setViews(eventsViews.getOrDefault(el.getId(), 0L));
            el.setConfirmedRequests(confirmedRequests.getOrDefault(el.getId(), 0L));
        });

        return eventDtoList;
    }

    public List<EventShortDto> mapToEventShortDto(Collection<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<EventShortDto> eventDtoList = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventsViews = getViews(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        eventDtoList.forEach(el -> {
            el.setViews(eventsViews.getOrDefault(el.getId(), 0L));
            el.setConfirmedRequests(confirmedRequests.getOrDefault(el.getId(), 0L));
        });

        return eventDtoList;
    }

    /*private void sendStats(String uri, String ip) {
        HitDto endpointHitRequestDto = HitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        statisticsClient.addHit(endpointHitRequestDto);
    }*/
}
