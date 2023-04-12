package pl.lpawlowski.chessapp.web.pieces

import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.game.engine.MovingStrategies

class Pawn(
    color: String,
    id: String,
    name: String
) : Piece(color, id, name, listOf(MovingStrategies.pawnMoving)) {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return MovingStrategies.pawnMoving.getAllPossibleDirections()
    }

    override fun canMoveMultipleSquares(): Boolean {
        return false
    }

    override fun getPieceIcon(): String {
        return ""
    }

    override fun canGoToTheSameField(): Boolean {
        return false
    }

    override fun getSpecialMoves(): List<MoveType> {
        return listOf(MoveType.EN_PASSANT, MoveType.MOVE_TWO, MoveType.PROM, MoveType.PAWN_CAPTURE)
    }

    override fun isPawn(): Boolean {
        return true
    }
    override fun toFenChar(): Char {
        return if (color == "white") 'P' else 'p'
    }
}