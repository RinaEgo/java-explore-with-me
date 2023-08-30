package ru.practicum.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {
    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public void createHit(@RequestBody HitDto hitDto) {
        statsService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam(name = "start")
                                   @DateTimeFormat(fallbackPatterns = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                   @RequestParam(name = "end")
                                   @DateTimeFormat(fallbackPatterns = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                   @RequestParam(name = "uris", required = false) List<String> uris,
                                   @RequestParam(name = "unique", defaultValue = "false") boolean unique) {

        return statsService.getStats(start, end, uris, unique);
    }
}
