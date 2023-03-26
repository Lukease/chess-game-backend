package pl.lpawlowski.chessapp.model.chess_possible_move.moving_startegy

import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d

class LineMovingStrategy : MovingStrategy() {
    override fun getAllPossibleDirections(): List<Vector2d> {
        return listOf(
            Vector2d(0, 1),
            Vector2d(0, -1),
            Vector2d(1, 0),
            Vector2d(-1, 0)
        )
    }
}