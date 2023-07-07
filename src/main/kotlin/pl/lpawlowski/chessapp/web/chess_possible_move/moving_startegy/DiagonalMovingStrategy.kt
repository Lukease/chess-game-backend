package pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy

import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d

class DiagonalMovingStrategy : MovingStrategy() {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return listOf(
            Vector2d(1, 1),
            Vector2d(-1, -1),
            Vector2d(1, -1),
            Vector2d(-1, 1),
        )
    }
}