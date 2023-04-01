package pl.lpawlowski.chessapp.model.pieces

class PieceDto (
    val color: String,
    val id: String,
    val name: String,
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