package com.formacom.batallanaval.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"game_id", "target_board_id", "row_index", "col_index"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_index", nullable = false)
    private Integer row;

    @Column(name = "col_index", nullable = false)
    private Integer col;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false, length = 20)
    private ShotResult result;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shooter_id", nullable = false)
    private User shooter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_board_id", nullable = false)
    private Board targetBoard;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public boolean getHit() {
    return true;
    }
}