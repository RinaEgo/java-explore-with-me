package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void createHit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
