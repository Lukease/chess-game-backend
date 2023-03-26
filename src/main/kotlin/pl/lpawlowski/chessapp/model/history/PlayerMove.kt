package pl.lpawlowski.chessapp.model.history

import pl.lpawlowski.chessapp.model.chess_possible_move.Move
import pl.lpawlowski.chessapp.model.pieces.Piece
import pl.lpawlowski.chessapp.model.suppliers.Field
import pl.lpawlowski.chessapp.model.suppliers.MoveType

class PlayerMove(
    val id: Long?,
    private val specialMove: MoveType?,
    private val fieldFrom: Field,
    val pieceFrom: Piece?,
    private val fieldTo: Field,
    val pieceTo: Piece?,
    val additionalField: Field?,
    private var fieldFromName: String,
    val secondMove: Move?,
    private val promotedPiece: Piece?,
    val isCheck: Boolean,
) {

    var nameOfMove: String = setNameOfMove(fieldFromName!!, promotedPiece)


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
        val icon: String = fieldFrom.piece?.getPieceIcon()!!
        val capture: String = when (fieldTo.piece != null) {
            true -> "x"
            false -> ""
        }

        return icon.plus(fieldFromName).plus(capture).plus(fieldTo.id.lowercase()).plus(specialMove?.name)
    }

    private fun setPiecePromotion(piece: Piece): String {
        return if (specialMove == MoveType.PROM) {
            piece.getPieceIcon()
        } else {
            ""
        }
    }
}