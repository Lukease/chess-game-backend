package pl.lpawlowski.chessapp.web.pieces

import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Rook(
    color: PlayerColor,
    id: String,
    name: PiecesNames
) : Piece(color, id, name, listOf(MovingStrategies.lineMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.lineMoving.getAllPossibleDirections()
    }

    override fun canMoveMultipleSquares(): Boolean {
        return true
    }

    override fun getPieceIcon(): String {
        return "♖"
    }

    override fun canGoToTheSameField(): Boolean {
        return true
    }

    override fun toFenChar(): Char {
        return if (color == PlayerColor.WHITE) 'R' else 'r'
    }
    override fun getSpecialMoves(): List<MoveType> {
        return listOf()
    }
}