package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.pieces.Piece
import pl.lpawlowski.chessapp.model.user.UserDto
import java.time.LocalDateTime

data class GameDto(
    val id: Long? = null,
    val moves: String,
    val lastMoveBlack: LocalDateTime? = null,
    val lastMoveWhite: LocalDateTime? = null,
    var timePerPlayerInSeconds: Int = 200,
    var pieces: List<Piece>,
    var gameStatus: String = GameStatus.CREATED.name,
    var whitePlayer: UserDto? = null,
    var blackPlayer: UserDto? = null
) {
    companion object {
        fun fromDomain(game: Game): GameDto {
            return GameDto(
                id = game.id,
                moves = game.moves,
                lastMoveBlack = game.lastMoveBlack,
                lastMoveWhite = game.lastMoveWhite,
                timePerPlayerInSeconds = game.timePerPlayerInSeconds,
                gameStatus = game.gameStatus,
                blackPlayer = game.blackPlayer?.let { UserDto.fromDomain(it) },
                whitePlayer = game.whitePlayer?.let { UserDto.fromDomain(it) },
                pieces = game.pieces
            )
        }
    }
}