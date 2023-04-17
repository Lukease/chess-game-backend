package pl.lpawlowski.chessapp.model.game

data class GameCreateRequest(
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)

data class GameMakeMoveRequest(
    val pieceFromId: String,
    val fieldToId: String,
    val promotedPieceName: String?
)

data class JoinGameRequest(
    val gameId: Long
)


