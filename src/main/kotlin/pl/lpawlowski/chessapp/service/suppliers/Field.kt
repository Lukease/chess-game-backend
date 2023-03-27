package pl.lpawlowski.chessapp.service.suppliers

import pl.lpawlowski.chessapp.model.chess_possible_move.Coordinate
import pl.lpawlowski.chessapp.model.pieces.Piece

class Field(
    private val rowNumber: Int,
    val id: String,
    private val columnNumber: Int,
    private val color: String,
    var piece: Piece?
) {
    private val coordinate: Coordinate = CoordinateService.getCoordinateById(id)
    fun setHasMoved() {

    }

    fun setKingCheck(isCheck: Boolean) {

    }

    fun getActive() {

    }

    fun setActiveTrash(active: Boolean) {

    }

    fun setActive(active: Boolean) {

    }

    fun setPossibleMove(correct: Boolean) {

    }

    fun restorePiece() {

    }

    fun setPiece(piece: Piece, active: Boolean) {
        this.piece = piece
        this.piece?.currentCoordinate = coordinate

        if (active) {

        }
    }

    fun removePiece() {
        piece = null
    }

}




