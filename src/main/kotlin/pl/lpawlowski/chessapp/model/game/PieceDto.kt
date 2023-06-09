package pl.lpawlowski.chessapp.model.game

import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.web.chess_possible_move.PossibleMove
import pl.lpawlowski.chessapp.web.pieces.*
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

        fun toDomain(pieceDto: PieceDto): Piece {
            val color = when (pieceDto.color) {
                "white" -> PlayerColor.WHITE
                else -> PlayerColor.BLACK
            }
            return when (pieceDto.name) {
                "Queen" -> Queen(color, pieceDto.id, PiecesNames.QUEEN)
                "King" -> King(color, pieceDto.id, PiecesNames.KING)
                "Bishop" -> Bishop(color, pieceDto.id, PiecesNames.BISHOP)
                "Rook" -> Rook(color, pieceDto.id, PiecesNames.ROOK)
                "Knight" -> Knight(color, pieceDto.id, PiecesNames.KNIGHT)
                else -> Pawn(color, pieceDto.id, PiecesNames.PAWN)
            }
        }
    }
}