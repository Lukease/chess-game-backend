package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.suppliers.MovingStrategies

class Knight(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.knightMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.knightMoving.getAllPossibleDirections()
    }

    override fun getImageUrl(): String {
        return "../../chess_icon/${this.color}-knight.svg"
    }

    override fun canMoveMultipleSquares(): Boolean {
        return false
    }

    override fun getPieceIcon(): String {
        return "♘"
    }

    override fun canGoToTheSameField(): Boolean {
        return true
    }
}