package pl.lpawlowski.chessapp.model.history

data class HistoryRequest(
    val gameId: Long,
    val moveId: Number,
    val playAsWhite: Boolean
)