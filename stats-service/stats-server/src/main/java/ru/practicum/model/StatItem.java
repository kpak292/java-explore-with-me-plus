package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "stats")
public class StatItem {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;

    @Column(name = "app")
    String app;

    @Column(name = "uri")
    String uri;

    @Column(name = "ip")
    String ip;

    @Column(name = "created_at")
    LocalDateTime created;
}
