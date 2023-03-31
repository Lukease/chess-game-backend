package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.model.pieces.PieceDto

data class MakeMoveResponse(
    val pieces: List<PieceDto>,
    val isCheck: Boolean,
    val historyOfMoves: String,
    val gameDto: GameDto
)