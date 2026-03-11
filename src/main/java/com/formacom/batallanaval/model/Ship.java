package com.formacom.batallanaval.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipType type;

    @Column(nullable = false)
    private Integer size;

    @Column(nullable = false)
    private Boolean sunk = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
}
