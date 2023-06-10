package pl.lpawlowski.chessapp.web.pieces

import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class King(
    color: PlayerColor,
    id: String,
    name: PiecesNames
) : Piece(color, id, name, listOf(MovingStrategies.diagonalMoving, MovingStrategies.lineMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.diagonalMoving.getAllPossibleDirections() + MovingStrategies.lineMoving.getAllPossibleDirections()
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

    override fun toFenChar(): Char {
        return if (color == PlayerColor.WHITE) 'K' else 'k'
    }

}