package pl.lpawlowski.chessapp.entities

import pl.lpawlowski.chessapp.game.GameStatus
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
    var timeLeftWhite: Int = 800
    var timeLeftBlack: Int = 800
    var gameStatus: String = GameStatus.CREATED.name
    var result: String? = null
    lateinit var fen: String
    lateinit var allMovesFen: String

    @ManyToOne
    @JoinColumn(name = "player_white_id")
    var whitePlayer: User? = null

    @ManyToOne
    @JoinColumn(name = "player_black_id")
    var blackPlayer: User? = null

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    var drawOffers: List<DrawOffers> = listOf()
}