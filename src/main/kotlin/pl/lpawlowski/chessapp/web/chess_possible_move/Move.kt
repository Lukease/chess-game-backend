package pl.lpawlowski.chessapp.web.chess_possible_move

import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.pieces.Piece

open class Move(
    val nameOfMove: String,
    val pieces: List<Piece>,
    val moveType: MoveType,
    val fieldFrom: String,
    val fieldTo: String,
    val promotedPiece: String
)