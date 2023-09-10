package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public RequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        if (event.getInitiator().getId().equals(userId)) {
            throw new NotAvailableException("Ошибка. Событие организовано пользователем.");
        }
        if (!requestRepository.findByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            throw new NotAvailableException("Ошибка. Запрос был отправлен ранее.");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new NotAvailableException("Ошибка. Событие не опубликовано.");
        }
        if (event.getConfirmedRequests() == (event.getParticipantLimit())) {
            throw new NotAvailableException("Ошибка. Достигнут лимит запросов на участие.");
        }

        Request request = new Request();

        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        request.setEvent(event);
        request.setRequester(user);
        request.setCreateDate(LocalDateTime.now());

        Request requestToSave = requestRepository.save(request);

        long confirmedRequest = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        event.setConfirmedRequests(confirmedRequest);
        eventRepository.save(event);

        return RequestMapper.toRequestDto(requestToSave);

    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getAll(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return requests
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с ID " + requestId + " не найдена."));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Ошибка. Заявка не принадлежит пользователю.");
        }
        if (request.getStatus().equals(RequestStatus.REJECTED) || request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("Ошибка. Заявка была отклонена ранее.");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return RequestMapper.toRequestDto(request);
    }
}
