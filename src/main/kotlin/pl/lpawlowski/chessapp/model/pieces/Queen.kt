package pl.lpawlowski.chessapp.model.pieces

import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Queen(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving, MovingStrategies.lineMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections() + MovingStrategies.lineMoving.getAllPossibleDirections()
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
    override fun toFenChar(): Char {
        return if (color == "white") 'Q' else 'q'
    }
     fun getQueenSpecialMoves(): List<MoveType> {
        return listOf(MoveType.NORMAL)
    }
}