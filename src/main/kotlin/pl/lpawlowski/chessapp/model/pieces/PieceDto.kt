package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.user.UserDto

class PieceDto (
    val color: String,
    val id: String,
    private val name: String,
    var possibleMoves: List<String> = mutableListOf()
) {
    companion object {
        fun fromDomain(piece: Piece): PieceDto {
            return PieceDto(
                id = piece.id,
                name = piece.name,
                color = piece.color,
                possibleMoves = piece.possibleMoves
                )
        }
    }
}