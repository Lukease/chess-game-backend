package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Bishop(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving)) {

    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections()
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

    override fun toFenChar(): Char {
        return if (color == "white") 'B' else 'b'
    }
}