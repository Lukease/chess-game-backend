package pl.lpawlowski.chessapp.model.game

data class MakeMoveResponse(
    val pieces: List<PieceDto>,
    val gameInfo: GameDto,
    val whoseTurn: String,
    val playerColor: String,
    val kingIsChecked: List<String>,
    val fieldFrom: String?,
    val fieldTo: String?
)

data class JoinGameResponse(
    val gameId: Long
)