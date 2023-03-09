package pl.lpawlowski.chessapp.model.game

data class Piece (
    val type: PieceType,
    val column: Int,
    val row: Int,
    val color: String,
    val possibleMoves: String
)

enum class PieceType {
    KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
}