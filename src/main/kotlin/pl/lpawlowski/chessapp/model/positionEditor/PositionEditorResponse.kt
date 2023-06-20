package pl.lpawlowski.chessapp.model.positionEditor

import pl.lpawlowski.chessapp.model.game.PieceDto

data class PositionEditorResponse(
    val pieces: List<PieceDto>,
    val kingIsChecked: List<String>
)