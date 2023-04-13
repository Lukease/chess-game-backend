package pl.lpawlowski.chessapp.web.chess_possible_move

import pl.lpawlowski.chessapp.model.game.PieceDto

open class Move (
     val nameOfMove: String,
     val pieces: List<PieceDto>
)