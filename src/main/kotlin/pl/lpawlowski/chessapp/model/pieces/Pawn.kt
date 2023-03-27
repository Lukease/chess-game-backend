package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.service.suppliers.MoveType
import pl.lpawlowski.chessapp.service.suppliers.MovingStrategies

class Pawn(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.pawnMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.pawnMoving.getAllPossibleDirections()
    }

    override fun getImageUrl(): String {
        return "../../chess_icon/${this.color}-Pawn.svg"
    }

    override fun canMoveMultipleSquares(): Boolean {
        return false
    }

    override fun getPieceIcon(): String {
        return ""
    }

    override fun canGoToTheSameField(): Boolean {
        return false
    }

    override fun getSpecialMoves(): List<MoveType> {
        return listOf(MoveType.EN_PASSANT, MoveType.MOVE_TWO, MoveType.PROM, MoveType.PAWN_CAPTURE)
    }

    override fun isPawn(): Boolean {
        return true
    }
}