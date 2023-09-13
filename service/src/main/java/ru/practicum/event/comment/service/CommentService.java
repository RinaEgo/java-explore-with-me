package ru.practicum.event.comment.service;

import ru.practicum.event.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long eventId, CommentDto commentDto, Long userId);

    CommentDto updateComment(Long commentId, CommentDto commentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getAllByUserId(Long userId);

    List<CommentDto> getAllByEventId(Long eventId);
}
