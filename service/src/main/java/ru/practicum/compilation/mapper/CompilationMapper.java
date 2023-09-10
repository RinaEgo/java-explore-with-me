package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> eventsShort = compilation.getEvents()
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        return CompilationDto
                .builder()
                .id(compilation.getId())
                .events(eventsShort)
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .build();
    }

    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();

        for (int i = 0; i < newCompilationDto.getEvents().size(); i++) {
            events.add(new Event());
            events.get(i).setId(newCompilationDto.getEvents().get(i));
        }

        return Compilation
                .builder()
                .id(newCompilationDto.getId())
                .events(events)
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.isPinned())
                .build();
    }
}
