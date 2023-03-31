package pl.lpawlowski.chessapp.game.engine

class MoveType(
    val name: String,
    val specialName: Boolean
) {
    companion object {
        val EN_PASSANT = MoveType("e.P.", false)
        val SMALL_CASTLE = MoveType("O-O", true)
        val BIG_CASTLE = MoveType("O-O-O", true)
        val MOVE_TWO = MoveType("", false)
        val PROM = MoveType("=", false)
        val PAWN_CAPTURE = MoveType("", false)
        val NORMAL = MoveType("", false)
    }
}