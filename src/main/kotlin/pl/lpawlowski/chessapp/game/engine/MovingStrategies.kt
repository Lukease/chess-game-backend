package pl.lpawlowski.chessapp.game.engine

import pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy.DiagonalMovingStrategy
import pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy.KnightMovingStrategy
import pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy.LineMovingStrategy
import pl.lpawlowski.chessapp.web.chess_possible_move.moving_startegy.PawnMovingStrategy

class MovingStrategies {
    companion object {
        val diagonalMoving = DiagonalMovingStrategy()
        val lineMoving = LineMovingStrategy()
        val knightMoving = KnightMovingStrategy()
        val pawnMoving = PawnMovingStrategy()
    }
}