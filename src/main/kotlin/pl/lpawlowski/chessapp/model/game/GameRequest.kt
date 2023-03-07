package pl.lpawlowski.chessapp.model.game

data class GameCreateRequest (
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)

data class GameMakeMoveRequest(
    val gameId: Long,
    val move: String
)

data class JoinGameRequest (
    val gameId: Long
)
