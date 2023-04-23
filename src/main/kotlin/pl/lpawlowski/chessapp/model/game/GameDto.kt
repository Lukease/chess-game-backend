package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.web.chess_possible_move.MoveHistory
import java.time.LocalDateTime

data class GameDto(
    val id: Long? = null,
    val moves: String,
    val lastMoveBlack: LocalDateTime? = null,
    val lastMoveWhite: LocalDateTime? = null,
    var timeLeftWhite: Int = 800,
    var timeLeftBlack: Int = 800,
    var timePerPlayerInSeconds: Int = 200,
    var gameStatus: String = GameStatus.CREATED.name,
    var fen: String = "",
    var whitePlayer: UserDto? = null,
    var blackPlayer: UserDto? = null,
) {
    companion object {
        fun fromDomain(game: Game): GameDto {
            return GameDto(
                id = game.id,
                moves = game.moves,
                lastMoveBlack = game.lastMoveBlack,
                lastMoveWhite = game.lastMoveWhite,
                timeLeftWhite = game.timeLeftWhite,
                timeLeftBlack = game.timeLeftBlack,
                timePerPlayerInSeconds = game.timePerPlayerInSeconds,
                gameStatus = game.gameStatus,
                fen = game.fen,
                blackPlayer = game.blackPlayer?.let { UserDto.toGameInfo(it) },
                whitePlayer = game.whitePlayer?.let { UserDto.toGameInfo(it) },
            )
        }
    }

}