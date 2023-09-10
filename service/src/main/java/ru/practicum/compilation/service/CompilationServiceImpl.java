package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        Compilation compilationS = compilationRepository.save(compilation);

        if (compilationS.getEvents().size() > 0) {
            compilationS.getEvents()
                    .replaceAll(this::getEvent);
        }

        return CompilationMapper.toCompilationDto(compilationS);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationDto updateCompilationDto) {

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compilationId + " не найдена."));

        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        if ((updateCompilationDto.isPinned() && !compilation.isPinned()) ||
                (!updateCompilationDto.isPinned() && compilation.isPinned())) {
            compilation.setPinned(updateCompilationDto.isPinned());
        }

        if (!updateCompilationDto.getEvents().isEmpty()) {
            compilationRepository.deleteCompilation(compilationId);
            List<Event> events = new ArrayList<>();

            if (updateCompilationDto.getEvents().size() > 0) {
                for (int i = 0; i < updateCompilationDto.getEvents().size(); i++) {
                    events.add(getEventById(updateCompilationDto.getEvents().get(i)));
                }
            }

            compilation.setEvents(events);
        }
        final Compilation compilationS = compilationRepository.save(compilation);

        return CompilationMapper.toCompilationDto(compilationS);
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {

        compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compilationId + " не найдена."));

        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(boolean pinned, int from, int size) {

        return compilationRepository.findAllByPinnedIs(pinned,
                        PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id")))
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compilationId) {

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compilationId + " не найдена."));

        return CompilationMapper.toCompilationDto(compilation);
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено."));
    }

    private Event getEvent(Event event) {
        return eventRepository.findById(event.getId())
                .orElseThrow(() -> new NotFoundException("Событие с id " + event.getId() + " не найдено."));
    }
}
