package pl.lpawlowski.chessapp.model.history

import pl.lpawlowski.chessapp.model.chess_possible_move.Move
import pl.lpawlowski.chessapp.model.pieces.Piece
import pl.lpawlowski.chessapp.game.engine.MoveType

class PlayerMove(
    val id: Long?,
    val specialMove: MoveType?,
    val fieldFrom: String,
    val pieceFrom: Piece?,
    val fieldTo: String,
    val pieceTo: Piece?,
    val additionalField: String?,
    var fieldFromName: String,
    val secondMove: Move?,
    val promotedPiece: Piece?,
    val isCheck: Boolean,
) {
//
//    var nameOfMove: String = setNameOfMove(fieldFromName!!, promotedPiece)
//
//
//    private fun setNameOfMove(fieldFromName: String, piece: Piece?): String {
//        return if (specialMove?.specialName == true) {
//            setSpecialMoveName().plus(setPiecePromotion(piece!!))
//        } else {
//            setIconOfMoveAndAddAllLetters(fieldFromName).plus(setPiecePromotion(piece!!))
//        }
//    }
//
//    private fun setSpecialMoveName(): String {
//        return specialMove?.name!!
//    }
//
//    private fun setIconOfMoveAndAddAllLetters(fieldFromName: String): String {
//        val icon: String = fieldFrom.piece?.getPieceIcon()!!
//        val capture: String = when (fieldTo.piece != null) {
//            true -> "x"
//            false -> ""
//        }
//
//        return icon.plus(fieldFromName).plus(capture).plus(fieldTo.id.lowercase()).plus(specialMove?.name)
//    }
//
//    private fun setPiecePromotion(piece: Piece): String {
//        return if (specialMove == MoveType.PROM) {
//            piece.getPieceIcon()
//        } else {
//            ""
//        }
//    }
}