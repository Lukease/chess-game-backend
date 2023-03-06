package pl.lpawlowski.chessapp.model.game

import java.time.LocalDateTime

data class GameMakeMoveRequest(
    val moves: String,
    val moveTime: LocalDateTime,
)