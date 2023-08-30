package ru.practicum.model;

import ru.practicum.HitDto;

public class HitMapper {

    public static Hit toHit(HitDto hitDto) {
        return Hit
                .builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
