package pl.lpawlowski.chessapp.service.suppliers

import pl.lpawlowski.chessapp.model.history.PlayerMove
import pl.lpawlowski.chessapp.model.pieces.Piece

class GameMoveService {
    var possibleMoves: List<Field> = emptyList()
    var previousMoveFields: List<Field> = emptyList()
    var arrayOfPossibleMoves: List<PlayerMove> = emptyList()
    var allFields: List<Field> = emptyList()
    var arrayOfMoves: List<PlayerMove> = emptyList()
    private var coveringKingFields: List<Field> = emptyList()
    var lastMove: PlayerMove? = null
    var kingCheck: Field? = null

    fun createAndMakeMove(field: Field){
//        val move = createMove(field, piece )
    }

    private fun createMove(field: Field, piece: Piece?){
//        val specialMoveName = if (piece != null) {
//            return MoveType.PROM
//        } else {
//            arrayOfPossibleMoves.find { it.fieldTo == field }?.specialMove
//        }
//        val fieldFrom = fieldFrom(field)

    }
}