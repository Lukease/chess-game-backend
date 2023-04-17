package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.web.pieces.Piece
import pl.lpawlowski.chessapp.web.chess_possible_move.PossibleMove
import java.util.*

class PieceDto(
    val color: String,
    var id: String,
    val name: String,
    var possibleMoves: List<PossibleMove> = mutableListOf(),
) {
    companion object {
        fun fromDomain(piece: Piece): PieceDto {
            val name = piece.name.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            return PieceDto(
                id = piece.id,
                name = name,
                color = piece.color.name.lowercase(),
                possibleMoves = piece.possibleMoves,
            )
        }
    }
}