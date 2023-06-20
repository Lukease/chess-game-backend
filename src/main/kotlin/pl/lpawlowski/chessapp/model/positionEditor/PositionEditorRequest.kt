package pl.lpawlowski.chessapp.model.positionEditor

import pl.lpawlowski.chessapp.model.game.PieceDto

data class ChangePositionOfPieceInPositionEditor(
    val piece: PieceDto,
    val newId: String,
    val isFromBoard: Boolean
)

data class NewFenRequest(
    val fen: String
)