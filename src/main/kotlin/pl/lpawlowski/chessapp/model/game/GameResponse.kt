package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.model.pieces.PieceDto

data class MakeMoveResponse(
    val pieces: List<PieceDto>,
    val gameDto: GameDto,
    val whoseTurn: String,
    val playerColor: String
)

data class JoinGameResponse(
    val gameId: Long
)