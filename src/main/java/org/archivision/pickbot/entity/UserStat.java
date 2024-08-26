package org.archivision.pickbot.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
@Data
public class UserStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_interaction_date", nullable = false)
    private LocalDateTime firstInteractionDate;

    @Column(name = "last_interaction_date", nullable = false)
    private LocalDateTime lastInteractionDate;

    @Column(name = "message_count", nullable = false)
    private Long messageCount;

    public UserStat() {}

    public UserStat(Long userId, String username, LocalDateTime firstInteractionDate) {
        this.userId = userId;
        this.username = username;
        this.firstInteractionDate = firstInteractionDate;
        this.lastInteractionDate = firstInteractionDate;
        this.messageCount = 0L;
    }

    public void updateInteraction(LocalDateTime interactionDate) {
        this.lastInteractionDate = interactionDate;
        this.messageCount++;
    }
}
