package com.SummerProject.Skote.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.Instant;
import java.util.UUID;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_presence")
public class UserPresence {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean isOnline;

    private Instant lastSeen;

    // Custom constructor for PresenceService
    public UserPresence(UUID userId, boolean isOnline, Instant lastSeen) {
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }
}
