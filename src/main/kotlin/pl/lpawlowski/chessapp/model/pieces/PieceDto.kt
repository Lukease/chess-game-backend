package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.SpecialMove

class PieceDto(
    val color: String,
    val id: String,
    val name: String,
    var possibleMoves: List<String> = mutableListOf(),
    val specialMoves: List<SpecialMove> = mutableListOf()
) {
    companion object {
        fun fromDomain(piece: Piece): PieceDto {
            return PieceDto(
                id = piece.id,
                name = piece.name,
                color = piece.color,
                possibleMoves = piece.possibleMoves,
                specialMoves = piece.specialMoves
            )
        }
    }
}