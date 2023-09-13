package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Hit;
import ru.practicum.model.HitMapper;
import ru.practicum.repository.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public HitDto createHit(HitDto hitDto) {
        Hit hit = hitMapper.toHit(hitDto);

        return hitMapper.toHitDto(statsRepository.save(hit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsDto> getStats(String start, String end, List<String> uris, String unique) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), dateTimeFormatter);
            endDate = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), dateTimeFormatter);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Некорректный формат даты.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Даты начала и окончания некорректны.");
        }
        boolean onlyUnique = Boolean.parseBoolean(unique);
        if (onlyUnique) {
            if (uris != null && !uris.isEmpty()) {
                uris.replaceAll(s -> s.replace("[", ""));
                uris.replaceAll(s -> s.replace("]", ""));
                return statsRepository.getUniqueWithUris(startDate, endDate, uris);
            } else {
                return statsRepository.getUniqueWithOutUris(startDate, endDate);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                uris.replaceAll(s -> s.replace("[", ""));
                uris.replaceAll(s -> s.replace("]", ""));
                return statsRepository.getWithUris(startDate, endDate, uris);
            } else {
                return statsRepository.getWithOutUris(startDate, endDate);
            }
        }
    }
}
