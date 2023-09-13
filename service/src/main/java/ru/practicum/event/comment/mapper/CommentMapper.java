package ru.practicum.event.comment.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.event.comment.dto.CommentDto;
import ru.practicum.event.comment.model.Comment;

@Mapper(componentModel = "spring")
@Component
public interface CommentMapper {

    Comment toComment(CommentDto commentDto);

    CommentDto toCommentDto(Comment comment);
}
