package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.web.pieces.Piece
import pl.lpawlowski.chessapp.web.chess_possible_move.SpecialMove

class PieceDto(
    val color: String,
    var id: String,
    val name: String,
    var possibleMoves: List<SpecialMove> = mutableListOf(),
) {
    companion object {
        fun fromDomain(piece: Piece): PieceDto {
            return PieceDto(
                id = piece.id,
                name = piece.name,
                color = piece.color,
                possibleMoves = piece.possibleMoves,
            )
        }
    }
}