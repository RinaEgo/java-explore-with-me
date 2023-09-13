package ru.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.service.StatsService;

import java.util.List;

@RestController
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<HitDto> createHit(@RequestBody HitDto hitDto) {

        return new ResponseEntity<>(statsService.createHit(hitDto), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatsDto>> getStats(@RequestParam String start,
                                                   @RequestParam String end,
                                                   @RequestParam(required = false) List<String> uris,
                                                   @RequestParam(defaultValue = "false") String unique) {

        return ResponseEntity.ok().body(statsService.getStats(start, end, uris, unique));
    }
}
