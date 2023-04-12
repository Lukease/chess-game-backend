package pl.lpawlowski.chessapp.web.pieces

import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Knight(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.knightMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.knightMoving.getAllPossibleDirections()
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
    override fun toFenChar(): Char {
        return if (color == "white") 'N' else 'n'
    }
    override fun getSpecialMoves(): List<MoveType> {
        return listOf(MoveType.NORMAL)
    }
}