package org.archivision.pickbot.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private Long startedBy;
    private Long endedBy;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;

    private Long chatId;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Place> places;

    public enum Status {
        ACTIVE, ENDED
    }
}

