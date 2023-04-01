package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.model.chess_possible_move.Coordinate
import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.pieces.*

@Service
class GameEngine {
    //    var possibleMoves: List<String> = emptyList()
//    var previousMoveFields: List<String> = emptyList()
//    var arrayOfPossibleMoves: List<PlayerMove> = emptyList()
    var allPieces: List<Piece> = emptyList()
//    var arrayOfMoves: List<PlayerMove> = emptyList()
//    private var coveringKingFields: List<String> = emptyList()
//    var lastMove: PlayerMove? = null
//    var kingCheck: String? = null

    fun createAndMakeMove(field: String) {
        val move = createMove(field)
    }

    private fun createMove(field: String) {
//        val specialMoveName = if (piece != null) {
//            return MoveType.PROM
//        } else {
//            arrayOfPossibleMoves.find { it.fieldTo == field }?.specialMove
//        }
//        val fieldFrom = fieldFrom(field)
    }

    fun getDefaultFen(): String {
        return "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
    }

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
                    val number: Int = index + columnPlus
                    val column: String = numberToColumn(number).toString()
                    val color = if (char.isUpperCase()) "white" else "black"
                    val id: String = column + (8 - row)

                    when (char.lowercaseChar()) {
                        'b' -> Bishop(color, id, "Bishop")
                        'k' -> King(color, id, "king")
                        'n' -> Knight(color, id, "Knight")
                        'p' -> Pawn(color, id, "Pawn")
                        'q' -> Queen(color, id, "Queen")
                        'r' -> Rook(color, id, "Rook")
                        else -> null
                    }
                }
            }
        }.filterNotNull()

        return piecesArray
    }

    fun convertPieceListToFen(pieces: List<Piece>): String {
        val ranks = mutableListOf<String>()
        for (row in 8 downTo 1) {
            var rank = ""
            var emptyField = 0
            for (column in 'a'..'h') {
                val piece = pieces.find { it.id == "$column$row" }
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
            }
            ranks.add(rank)
        }
        return ranks.joinToString("/")
    }


    fun getAllPossibleMovesOfPlayer(piecesArray: List<Piece>, color: String): List<PieceDto> {
        allPieces = piecesArray
        val playerPieces: List<Piece> = piecesArray.mapNotNull { piece: Piece ->
            if (piece.color == color) {
                piece
            } else {
                null
            }
        }

        return getAllPossibleMoves(playerPieces, piecesArray)
    }

    private fun getAllPossibleMoves(playerPieces: List<Piece>, allPieces: List<Piece>): List<PieceDto> {
        return playerPieces.map { piece: Piece ->
            val correctId: List<String> = piece.getAllPossibleDirectionsWithColor()
                .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                .flatten()

            val pieceMoves = if (piece.isPawn()) {
                correctId + isPawnCapturePossible(piece)
            } else {
                correctId
            }

            piece.possibleMoves = pieceMoves

            PieceDto.fromDomain(piece)
        }
    }

    private fun isPawnCapturePossible(piece: Piece): List<String> {
        val direction = if (piece.color == "white") 1 else -1
//        val currentCoordinate: Coordinate = piece.currentCoordinate
        val currentCoordinate: Coordinate = Coordinate(1, 2, "A", "2")
        val leftX: Int = currentCoordinate.x - 1
        val rightX: Int = currentCoordinate.x + 1
        val fieldNumber: Int = currentCoordinate.y + direction
        val leftSideCoordinate: String? = getFieldByXY(leftX, fieldNumber)
        val rightSideCoordinate: String? = getFieldByXY(rightX, fieldNumber)
        val fieldsId = listOfNotNull(leftSideCoordinate, rightSideCoordinate).map { getPieceById(it, allPieces) }
            .filter { it?.color != piece.color }

        return fieldsId.filterNotNull().map { it.id }
    }

    private fun getAllPossibleMovesFromDirection(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        return if (currentPiece.canMoveMultipleSquares()) {
            getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(currentPiece, direction, allPieces)
        } else {
            getPossibleMovesOfPawnAndKing(currentPiece.id, currentPiece, direction, allPieces)
        }
    }

    private fun getPossibleMovesOfPawns(piece: Piece?, currentPiece: Piece): List<String> {
        return if (piece?.color == currentPiece.color) {
            listOf()
        } else {
            listOf(piece!!.id)
        }
    }

    private fun getPossibleMovesOfPawnAndKing(
        field: String,
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        val vector: Vector2d = convertIdToVector(field)
        val x = vector.x + direction.x
        val y = vector.y + direction.y
        val currentField = getFieldByXY(x, y)
        if (currentField != null) {
            val piece: Piece? = getPieceById(currentField!!, allPieces)
            val isPawn = currentPiece?.isPawn()

            return if (isPawn == true) {
                if (piece != null && piece.color == currentPiece.color) {
                    listOf()
                } else {
                    listOf(currentField)
                }
            } else {
                if (piece != null) {
                    listOf()
                } else {
                    listOf(currentField)
                }
            }
        } else {
            return listOf()
        }
    }

    private fun getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        var counter = 1
        var canGoFurther = true
        val vector: Vector2d = convertIdToVector(currentPiece.id)
        var x = direction.x * counter
        var y = direction.y * counter
        var field = getFieldByXY(vector.x + x, vector.y + y)
        if (field != null) {
            var piece = getPieceById(field, allPieces)

            val fields: MutableList<String> = mutableListOf()
            while (field != null && piece?.color != currentPiece.color && canGoFurther) {
                if (piece != null) {
                    canGoFurther = false
                }
                counter++
                fields.add(field)

                field = getFieldByXY(vector.x + (direction.x * counter), vector.y + (direction.y * counter))
                piece = getPieceById(field!!, allPieces)
            }

            return fields
        } else {
            return listOf()
        }
    }

    private fun getFieldByXY(x: Int, y: Int): String? {
        val column = numberToColumn(x)
        val row = y.toString()

        return generateFields().find { field -> field[0] == column && field[1] == row[0] }
    }

    private fun getFieldByID(id: String): String? {

        return generateFields().find { field -> field == id }
    }

    private fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0]) - 64
        val y = id[1].digitToInt()

        return Vector2d(x, y)
    }

    private fun generateFields(): List<String> {
        val rows = listOf("1", "2", "3", "4", "5", "6", "7", "8")
        val columns = listOf("A", "B", "C", "D", "E", "F", "G", "H")

        val result = mutableListOf<String>()
        for (row in rows) {
            for (column in columns) {
                result.add(column + row)
            }
        }

        return result
    }

    private fun charToNumber(column: Char): Int {
        return column.code
    }

    private fun numberToColumn(column: Int): Char {
        return (column + 64).toChar()
    }

    private fun getPieceById(id: String, allPieces: List<Piece>): Piece? {
        return allPieces.find { it.id == id }
    }
}