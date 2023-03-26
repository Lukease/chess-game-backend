package pl.lpawlowski.chessapp.model.chess_possible_move.moving_startegy

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d

class KnightMovingStrategy : MovingStrategy() {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return listOf(
            Vector2d(-1, 2),
            Vector2d(-1, -2),
            Vector2d(1, -2),
            Vector2d(1, 2),
            Vector2d(2, 1),
            Vector2d(2, -1),
            Vector2d(-2, 1),
            Vector2d(-2, -1)
        )
    }
}