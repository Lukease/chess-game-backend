package pl.lpawlowski.chessapp.model.game

import java.time.LocalDateTime

data class GameCreateRequest(
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)

data class GameMakeMoveRequest(
    val move: String
)

data class JoinGameRequest(
    val gameId: Long
)

data class MakeMoveResponse(
    val pieces: List<Piece>,
    val isCheck: Boolean,
    val whoseTurn: String,
    val lastPlayerMove: LocalDateTime,
    val historyOfMoves: String
)

data class LeaveGameRequest(
    val gameId: Long
)


