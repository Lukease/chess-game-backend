package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.suppliers.MoveType
import pl.lpawlowski.chessapp.model.suppliers.MovingStrategies

class King(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving, MovingStrategies.lineMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections() + MovingStrategies.lineMoving.getAllPossibleDirections()
    }

    override fun getImageUrl(): String {
        return "../../chess_icon/${this.color}-king.svg"
    }

    override fun canDelete(): Boolean {
        return false
    }

    override fun canMoveMultipleSquares(): Boolean {
        return false
    }

    override fun getPieceIcon(): String {
        return "♔"
    }

    override fun canGoToTheSameField(): Boolean {
        return false
    }

    override fun getSpecialMoves(): List<MoveType> {
        return listOf(MoveType.SMALL_CASTLE, MoveType.BIG_CASTLE)
    }

    override fun isKing(): Boolean {
        return true
    }
}