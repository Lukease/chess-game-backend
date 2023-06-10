package pl.lpawlowski.chessapp.game.engine

enum class MoveType(
    val historyNotation: String,
    val specialName: Boolean
) {
    EN_PASSANT("e.P.", false),
    SMALL_CASTLE("O-O", true),
    BIG_CASTLE("O-O-O", true),
    MOVE_TWO("", false),
    PROM("=", false),
    PAWN_CAPTURE("", false),
    NORMAL("", false)
}
