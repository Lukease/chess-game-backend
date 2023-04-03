package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.model.pieces.PieceDto

data class GameCreateRequest(
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)

data class GameMakeMoveRequest(
    val moveId: String,
    val piece: PieceDto
)

data class JoinGameRequest(
    val gameId: Long
)

data class LeaveGameRequest(
    val gameId: Long
)


