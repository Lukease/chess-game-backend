package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.user.UserDto
import java.time.LocalDateTime

class GameDto(
    val id: Long? = null,
    val moves: String,
    val lastMoveBlack: LocalDateTime,
    val lastMoveWhite: LocalDateTime
) {
    companion object {
        fun fromDomain(game: Game): GameDto {
            return GameDto(
                id = game.id,
                moves = game.moves,
                lastMoveBlack = game.lastMoveBlack,
                lastMoveWhite = game.lastMoveWhite,
            )
        }
    }
}