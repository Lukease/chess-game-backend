package pl.lpawlowski.chessapp.web.pieces

import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Queen(
    color: PlayerColor,
    id: String,
    name: PiecesNames
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
        return if (color == PlayerColor.WHITE) 'Q' else 'q'
    }
    override fun getSpecialMoves(): List<MoveType> {
        return listOf(MoveType.NORMAL)
    }
}