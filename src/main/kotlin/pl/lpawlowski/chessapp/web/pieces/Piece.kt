package pl.lpawlowski.chessapp.web.pieces


import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.web.chess_possible_move.Coordinate
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy.MovingStrategy
import pl.lpawlowski.chessapp.game.engine.CoordinateService
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.web.chess_possible_move.PossibleMove

abstract class Piece(
    val color: PlayerColor,
    var id: String,
    val name: PiecesNames,
    val movingStrategies: List<MovingStrategy>,
    var possibleMoves: List<PossibleMove> = mutableListOf(),
) {
    var currentCoordinate: Coordinate = CoordinateService.getCoordinateById(id)
    val startingCoordinate: Coordinate = currentCoordinate
    var hasMoved: Boolean = false
    abstract fun getAllPossibleDirections(): List<Vector2d>

    open fun canDelete(): Boolean {
        return true
    }

    fun isInStartingPosition(): Boolean {
        return startingCoordinate == currentCoordinate
    }

    fun setHasMoved(): Boolean {
        return (hasMoved).also { hasMoved = true }
    }

    abstract fun canMoveMultipleSquares(): Boolean

    abstract fun getPieceIcon(): String
    abstract fun canGoToTheSameField(): Boolean
    open fun isPawn(): Boolean {
        return false
    }

    open fun isKing(): Boolean {
        return false
    }

    abstract fun getSpecialMoves(): List<MoveType>
    abstract fun toFenChar(): Char
}