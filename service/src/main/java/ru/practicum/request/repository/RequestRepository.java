package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findByEventIdAndStatus(Long id, RequestStatus status);

    List<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByStatusAndEventIdIn(RequestStatus status, Collection<Long> eventIds);
}
