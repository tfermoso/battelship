package com.formacom.batallanaval.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ship_positions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"board_id", "row_index", "col_index"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_index", nullable = false)
    private Integer row;

    @Column(name = "col_index", nullable = false)
    private Integer col;

    @Column(nullable = false)
    private Boolean hit = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
}