package pl.lpawlowski.chessapp.entities

import java.time.LocalDateTime
import javax.persistence.*

@Table(name = "games")
@Entity
class Game {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null

    var moves: String = ""
    var lastMoveBlack: LocalDateTime? = null
    var lastMoveWhite: LocalDateTime? = null
    var timePerPlayerInSeconds: Int = 800

    @ManyToOne
    @JoinColumn(name = "player_white_id")
    var whitePlayer: User? = null

    @ManyToOne
    @JoinColumn(name = "player_black_id")
    var blackPlayer: User? = null
}