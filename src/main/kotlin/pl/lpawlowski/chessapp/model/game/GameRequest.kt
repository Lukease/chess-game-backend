package pl.lpawlowski.chessapp.model.game

data class GameCreateRequest(
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)

data class GameMakeMoveRequest(
    val specialMove: Boolean,
    val pieceFrom: String,
    val fieldToId: String,
    val promotedPiece: PieceDto?
)

data class JoinGameRequest(
    val gameId: Long
)


