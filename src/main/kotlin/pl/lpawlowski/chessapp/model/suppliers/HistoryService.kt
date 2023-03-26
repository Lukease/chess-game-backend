package pl.lpawlowski.chessapp.model.suppliers

import pl.lpawlowski.chessapp.model.chess_possible_move.Move
import pl.lpawlowski.chessapp.model.pieces.Pawn

class HistoryService {
//    var historyOfMovesBackTo = mutableListOf<PieceMove>()
//    var arrayOfMoves = mutableListOf<PieceMove>()
//    var kingCheck: Field? = null
//    private var allFields = mutableListOf<Field>()
//    var previousMoveFields = mutableListOf<Field>()
//
//    fun addFieldToHistoryService(field: Field) {
//        allFields.add(field)
//    }
//
//    fun setArrayOfMoves(moves: List<PieceMove>) {
//        arrayOfMoves = moves.toMutableList()
//
//        val arrayLength = moves.size - 1
//        val lastMove = moves[arrayLength]
//
//        previousMoveFields = mutableListOf(lastMove.fieldFrom, lastMove.fieldTo)
//    }
//
//    fun renderHistory(moveId: Int) {
//
//    }
//
//    fun backToActualPositionIfHistoryElementClicked() {
//        if (historyOfMovesBackTo.isNotEmpty()) {
//            historyOfMovesBackTo.reversed().forEach { move ->
//                removeOldKingCheckAndAddNew(move)
//
//                if (move.secondMove != null) {
//                    makeMoveFromHistory(move.secondMove!!)
//                }
//
//                if (move.additionalField != null) {
//                    removePieceFromField(move.additionalField!!)
//                }
//
//                makeMoveFromHistory(move)
//
//                if (move.promotedPiece != null) {
//                    move.fieldTo.setPiece(move.promotedPiece!!, true)
//                }
//            }
//        }
//    }
//
//    private fun removePieceFromField(field: Field) {
//        field.removePiece()
//    }
//
//    private fun makeMoveFromHistory(move: Move) {
//        disableHighlightHistoryFields()
//        move.fieldTo.setPiece(move.pieceFrom!!, true)
//        move.fieldFrom.removePiece()
//        previousMoveFields = mutableListOf(move.fieldFrom, move.fieldTo)
//        highlightHistoryLastMoveFields()
//    }
//
//    private fun disableHighlightHistoryFields() {
//        previousMoveFields.forEach { field: Field ->
//            field.setActive((false))
//        }
//    }
//
//    fun addHistoryOfMovesFromClickedElement(moveId: Int) {
//        historyOfMovesBackTo = arrayOfMoves.filter { move -> move.id!! > moveId }.reversed().toMutableList()
//    }
//
//    fun undoneMoveToClickedElement() {
//        val arraySize = historyOfMovesBackTo.size - 1
//        historyOfMovesBackTo.forEachIndexed { index, move ->
//            removeOldKingCheckAndAddNew(move)
//
//            if (move.secondMove != null) {
//                undoMove(move.secondMove!!)
//            }
//
//            if (move.additionalField != null) {
//                undoneMoveForAdditionalField(move)
//            }
//
//            if (move.promotedPiece != null) {
//                move.fieldFrom.setPiece(move.pieceFrom!!, true)
//            }
//
//            undoMove(move)
//
//            if (index == arraySize) {
//                makeMoveFromHistory(move)
//            }
//        }
//    }
//
//    private fun undoMove(move: Move) {
//        disableHighlightHistoryFields()
//        move.fieldFrom.setPiece(move.fieldFrom, move.fieldTo)
//        highlightHistoryLastMoveFields
//
//        if (move.pieceTo != null) {
//            move.fieldTo.setPiece(move.pieceTo!!, true)
//        } else {
//            move.fieldTo.removePiece()
//        }
//    }
//
//    private fun undoneMoveForAdditionalField(move: Move){
//        val pawn = createPawnAdditionalField(move)
//
//        move.additionalField!.setPiece(pawn, false)
//    }
//
//    fun createPawnForAdditionalField(move: Move): Pawn{
//        val coordinate = move.additionalField!!.coordinate
//        val color = if (move.fieldTo.piece?.color == "white") "balck" else "white"
//
//        return Pawn(color.coordinate.boardColumn + coordinate.boardRow, "Pawn")
//    }
}