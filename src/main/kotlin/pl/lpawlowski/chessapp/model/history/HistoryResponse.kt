package pl.lpawlowski.chessapp.model.history

import pl.lpawlowski.chessapp.model.game.GameDto

data class HistoryResponse (
    val fen: String,
    val fieldFrom: String?,
    val fieldTo: String?
)

data class AllHistoryGamesResponse (
    val gamesAsWhite: List<GameDto>,
    val gamesAsBlack: List<GameDto>
)

