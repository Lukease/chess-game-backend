package pl.lpawlowski.chessapp.web.chess_possible_move

import pl.lpawlowski.chessapp.game.engine.MoveType

data class MoveHistory(
    val nameOfMove: String,
    val moveType: MoveType,
    val fieldFrom: String,
    val fieldTo: String,
    val promotedPiece: String?,
    val isCheck: Boolean
)