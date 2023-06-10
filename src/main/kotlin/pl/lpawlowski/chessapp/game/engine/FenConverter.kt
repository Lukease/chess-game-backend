package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Component
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.web.pieces.*

@Component
class FenConverter {
    fun convertFenToPiecesList(fen: String): List<Piece> {
        val split = fen.split("/")
        val piecesArray = split.flatMapIndexed { row: Int, it: String ->
            val splitFen = it.toCharArray()
            var columnPlus = 1

            splitFen.mapIndexed { index, char: Char ->
                if (char.isDigit()) {
                    columnPlus += char.digitToInt()
                    null

                } else {
                    val number: Int =  columnPlus
                    val column: String = numberToChar(number).toString()
                    val color = if (char.isUpperCase()) PlayerColor.WHITE else PlayerColor.BLACK
                    val id: String = column + (8 - row)

                    columnPlus++
                    getPieceByChar(char.lowercaseChar(), color, id)
                }
            }
        }.filterNotNull()

        return piecesArray
    }

    fun getPieceByChar(char: Char, color: PlayerColor, id: String): Piece {
        return when (char.lowercaseChar()) {
            'b' -> Bishop(color, id, PiecesNames.BISHOP)
            'k' -> King(color, id, PiecesNames.KING)
            'n' -> Knight(color, id, PiecesNames.KNIGHT)
            'p' -> Pawn(color, id, PiecesNames.PAWN)
            'q' -> Queen(color, id, PiecesNames.QUEEN)
            'r' -> Rook(color, id, PiecesNames.ROOK)
            else -> throw WrongMove("Piece with char: ${char.lowercaseChar()} not found")
        }
    }

    fun convertPieceListToFen(pieces: List<Piece>): String {
        val fen = mutableListOf<String>()
        var emptyField = 0
        for (row in 8 downTo 1) {
            var rank = ""
            for (column in 'a'..'h') {
                val piece = pieces.find { it.id == "${column.uppercase()}$row" }
                if (piece != null) {
                    if (emptyField > 0) {
                        rank += emptyField.toString()
                        emptyField = 0
                    }
                    rank += piece.toFenChar()
                } else {
                    emptyField++
                }
            }
            if (emptyField > 0) {
                rank += emptyField.toString()
                emptyField = 0
            }
            fen.add(rank)
        }
        return fen.joinToString("/")
    }

    private fun numberToChar(column: Int): Char = (column + 64).toChar()
}