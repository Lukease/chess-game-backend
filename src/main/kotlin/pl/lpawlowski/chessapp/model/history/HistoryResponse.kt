package pl.lpawlowski.chessapp.model.history

import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.game.PieceDto

data class HistoryResponse(
    val pieces: List<PieceDto>,
    val fieldFromTo: List<String>,
    val gameInfo: GameDto
)

data class AllHistoryGamesResponse(
    val gamesAsWhite: List<GameDto>,
    val gamesAsBlack: List<GameDto>
)

