package pl.lpawlowski.chessapp.model.game

data class GameCreateRequest (
    val isWhitePlayer: Boolean,
    val timePerPlayerInSeconds: Int
)