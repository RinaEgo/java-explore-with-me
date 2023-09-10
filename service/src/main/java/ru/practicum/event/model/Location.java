package ru.practicum.event.model;

import lombok.*;

import javax.persistence.Embeddable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Embeddable
public class Location {
    private Double lat;
    private Double lon;
}
