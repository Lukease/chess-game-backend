package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.entities.Game
import java.time.LocalDateTime

class GameDto(
    val id: Long? = null,
    val moves: String,
    val lastMoveBlack: LocalDateTime? = null,
    val lastMoveWhite: LocalDateTime? = null,
    var timePerPlayerInSeconds: Int = 800,
    var gameStatus: String = "waiting room"
) {
    companion object {
        fun fromDomain(game: Game): GameDto {
            return GameDto(
                id = game.id,
                moves = game.moves,
                lastMoveBlack = game.lastMoveBlack,
                lastMoveWhite = game.lastMoveWhite,
                timePerPlayerInSeconds = game.timePerPlayerInSeconds,
                gameStatus = game.gameStatus
            )
        }
    }
}