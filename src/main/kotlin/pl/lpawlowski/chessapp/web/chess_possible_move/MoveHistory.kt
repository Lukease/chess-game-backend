package pl.lpawlowski.chessapp.web.chess_possible_move

class MoveHistory(
    val currentName: String,
    currentId: String,
    val nameBefore: String,
    val idBefore: String,
    val idInArray: Int,
    isCheck: Boolean,
    val specialMove: String,
) : Move(isCheck, currentId)