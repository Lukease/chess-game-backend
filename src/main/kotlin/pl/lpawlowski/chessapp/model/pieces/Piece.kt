package pl.lpawlowski.chessapp.model.pieces


import pl.lpawlowski.chessapp.model.chess_possible_move.Coordinate
import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.chess_possible_move.moving_startegy.MovingStrategy
import pl.lpawlowski.chessapp.service.suppliers.CoordinateService
import pl.lpawlowski.chessapp.service.suppliers.MoveType

abstract class Piece(
    val color: String,
    id: String,
    private val name: String,
    val movingStrategies: List<MovingStrategy>,
) {
    var currentCoordinate: Coordinate = CoordinateService.getCoordinateById(id)
    private val startingCoordinate: Coordinate = currentCoordinate
    private var hasMoved: Boolean = false
    abstract fun getAllPossibleDirections(): List<Vector2d>

    fun getAllPossibleDirectionsWithColor(): Any {
        return if (name == "Pawn" && color == "black") {
            getAllPossibleDirections().map { Vector2d(it.x, it.y * -1) }.toTypedArray()
        } else {
            getAllPossibleDirections()
        }
    }

    abstract fun getImageUrl(): String

    open fun canDelete(): Boolean {
        return true
    }

    fun isInStartingPosition(): Boolean {
        return startingCoordinate == currentCoordinate
    }

    open fun getSpecialMoves(): List<MoveType> {
        return emptyList()
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
}