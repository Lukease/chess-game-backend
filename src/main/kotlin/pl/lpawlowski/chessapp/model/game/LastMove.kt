package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.pieces.Piece

class LastMove(
    val moveType: MoveType,
    val fieldFrom: String,
    val fieldTo: String,
    val pieceList: List<Piece>
)