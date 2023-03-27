package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.service.suppliers.MovingStrategies

class Bishop(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections()
    }

    override fun getImageUrl(): String {
        return "../../chess_icon/${this.color}-Bishop.svg"
    }

    override fun canMoveMultipleSquares(): Boolean {
        return true
    }

    override fun getPieceIcon(): String {
        return "♗"
    }

    override fun canGoToTheSameField(): Boolean {
        return true
    }
}