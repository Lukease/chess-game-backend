package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.service.suppliers.MovingStrategies

class Queen(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving, MovingStrategies.lineMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections() + MovingStrategies.lineMoving.getAllPossibleDirections()
    }

    override fun getImageUrl(): String {
        return "../../chess_icon/${this.color}-Queen.svg"
    }

    override fun canMoveMultipleSquares(): Boolean {
        return true
    }

    override fun getPieceIcon(): String {
        return "♕"
    }

    override fun canGoToTheSameField(): Boolean {
        return false
    }
}