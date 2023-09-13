package ru.practicum.event.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.comment.dto.CommentDto;
import ru.practicum.event.comment.mapper.CommentMapper;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long eventId, CommentDto commentDto, Long userId) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        Optional<Request> request = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (request.isEmpty()) {
            throw new NotFoundException("Ошибка. Пользователь с ID " + userId + " не участвовал в событии с ID " + eventId);
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setEvent(event);

        Comment commentToSave = commentRepository.save(comment);
        return commentMapper.toCommentDto(commentToSave);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, CommentDto commentDto) {

        Comment commentToUpdate = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден."));

        commentToUpdate.setText(commentDto.getText());
        commentToUpdate.setCreated(LocalDateTime.now());

        return commentMapper.toCommentDto(commentRepository.save(commentToUpdate));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден."));

        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByUserId(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Comment> comments = commentRepository.findAllByAuthorId(userId);

        return comments
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByEventId(Long eventId) {

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено."));

        return commentRepository.findAllByEventId(eventId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
