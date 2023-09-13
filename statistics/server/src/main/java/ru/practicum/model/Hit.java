package ru.practicum.model;

import jdk.jfr.Timestamp;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hits")
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String app;

    private String uri;

    private String ip;

    @Timestamp
    private LocalDateTime timestamp;
}
