package pl.lpawlowski.chessapp.model.chess_possible_move.moving_startegy

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d

class PawnMovingStrategy : MovingStrategy() {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return listOf(Vector2d(0, 1))
    }
}