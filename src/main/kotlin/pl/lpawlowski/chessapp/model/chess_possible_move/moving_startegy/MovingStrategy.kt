package pl.lpawlowski.chessapp.model.chess_possible_move.moving_startegy

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d

abstract class MovingStrategy {
    abstract fun getAllPossibleDirections(): List<Vector2d>
}