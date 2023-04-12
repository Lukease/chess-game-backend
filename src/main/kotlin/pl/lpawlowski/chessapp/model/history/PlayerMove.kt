package pl.lpawlowski.chessapp.model.history

import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.pieces.Piece
import pl.lpawlowski.chessapp.game.engine.MoveType

class PlayerMove(
    val id: Long?,
    val specialMove: MoveType?,
    val pieceFrom: Piece?,
    val pieceTo: Piece?,
    val additionalField: String?,
    var fieldFromName: String,
    val secondMove: Move?,
    val promotedPiece: Piece?,
    var isCheck: Boolean,
) {
    var nameOfMove: String = setNameOfMove(fieldFromName, promotedPiece)
    private fun setNameOfMove(fieldFromName: String, piece: Piece?): String {
        return if (specialMove?.specialName == true) {
            setSpecialMoveName().plus(setPiecePromotion(piece!!))
        } else {
            setIconOfMoveAndAddAllLetters(fieldFromName).plus(setPiecePromotion(piece!!))
        }
    }

    private fun setSpecialMoveName(): String {
        return specialMove?.name!!
    }

    private fun setIconOfMoveAndAddAllLetters(fieldFromName: String): String {
        val icon: String = pieceFrom?.getPieceIcon()!!
        val capture: String = when (pieceTo != null) {
            true -> "x"
            false -> ""
        }

        return icon.plus(fieldFromName).plus(capture).plus(pieceTo?.id?.lowercase()).plus(specialMove?.name)
    }

    private fun setPiecePromotion(piece: Piece): String {
        return if (specialMove == MoveType.PROM) {
            piece.getPieceIcon()
        } else {
            ""
        }
    }
}