package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.web.chess_possible_move.SpecialMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.history.PlayerMove
import pl.lpawlowski.chessapp.web.pieces.*

@Service
class GameEngine {
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
                    columnPlus = char.digitToInt()
                    null

                } else {
                    val number: Int = index + columnPlus
                    val column: String = numberToChar(number).toString()
                    val color = if (char.isUpperCase()) "white" else "black"
                    val id: String = column + (8 - row)
                    when (char.lowercaseChar()) {
                        'b' -> Bishop(color, id, "Bishop")
                        'k' -> King(color, id, "King")
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

//    fun addKingIsCheckedAndChangeNameOfMove(move: PlayerMove, color: String, piecesArray: List<Piece>): PlayerMove? {
//        if (getTheKingIsChecked(color, piecesArray)) {
//
//            move.nameOfMove += "+"
//            return move
//        }
//        return null
//    }

    fun getTheKingIsChecked(color: String, piecesArray: List<Piece>): Boolean {
        val king = piecesArray.find { it.color == color && it is King }
        val enemyColor = if (color == "white") "black" else "white"
        val allPossibleMovesOfEnemy = getAllPossibleMovesOfPlayer(piecesArray, enemyColor)

        return checkKingPositionIsChecked(allPossibleMovesOfEnemy, king!!)
    }

    private fun checkKingPositionIsChecked(allPossibleMovesOfEnemy: List<PieceDto>, king: Piece): Boolean {
        return allPossibleMovesOfEnemy.find { piece ->
            piece.possibleMoves.any { it.fieldId == king.id }
        } != null
    }


    fun getAllPossibleMovesOfPlayer(piecesArray: List<Piece>, color: String): List<PieceDto> {
        val playerPieces: List<Piece> = piecesArray.mapNotNull { piece: Piece ->
            if (piece.color == color) {
                piece
            } else {
                null
            }
        }

        return getAllPossibleMoves(playerPieces, piecesArray)
    }

    fun getEnemyPieces(piecesArray: List<Piece>, color: String): List<PieceDto> {
        return piecesArray.mapNotNull { piece: Piece ->
            if (piece.color != color) {
                PieceDto.fromDomain(piece)
            } else {
                null
            }
        }
    }

    private fun getAllPossibleMoves(playerPieces: List<Piece>, allPieces: List<Piece>): List<PieceDto> {
        return playerPieces.map { piece: Piece ->
            val correctId: List<String> = piece.getAllPossibleDirectionsWithColor()
                .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                .flatten()

            val pieceMoves = if (piece.isPawn()) {
                correctId + isPawnCapturePossible(piece, allPieces)
                //todo repair isPawnCapturePossible
            } else {
                correctId
            }
            val possibleNormalMoves = pieceMoves.map { SpecialMove(MoveType.NORMAL, it) }

            piece.possibleMoves = possibleNormalMoves
            getSpecialMovesForPiece(piece, allPieces)
            PieceDto.fromDomain(piece)
        }
    }

    private fun getAllPossibleMovesFromDirection(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        return if (currentPiece.canMoveMultipleSquares()) {
            getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(currentPiece, direction, allPieces)
        } else {
            getPossibleMovesOfPawnAndKing(currentPiece, direction, allPieces)
        }
    }

    //    private fun getPossibleMovesOfPawns(piece: Piece?, currentPiece: Piece): List<String> {
//        return if (piece?.color == currentPiece.color) {
//            listOf()
//        } else {
//            listOf(piece!!.id)
//        }
//    }
    private fun getPossibleMovesOfPawnAndKing(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        val vector: Vector2d = convertIdToVector(currentPiece.id)
        val letter = numberToChar(vector.x + direction.x).toString()
        val number = vector.y + direction.y
        val currentField = getFieldByID("${letter}${number}")

            return currentField?.let { field ->
                val piece: Piece? = getPieceById(field, allPieces)
                if (currentPiece.isPawn()) {
                    if (piece?.color != currentPiece.color) {
                        listOf(field)
                    } else {
                        emptyList()
                    }
                } else {
                    if (piece != null) {
                        if (piece.color != currentPiece.color) {
                            listOf(field)
                        } else {
                            emptyList()
                        }
                    } else {
                        listOf(field)
                    }
                }
            } ?: emptyList()
    }

    private fun getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        var counter = 1
        var canGoFurther = true
        val vector: Vector2d = convertIdToVector(currentPiece.id)
        val x = direction.x * counter
        val y = direction.y * counter
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
                if (field != null) {
                    piece = getPieceById(field, allPieces)
                }
            }

            return fields
        } else {
            return listOf()
        }
    }


    fun getSpecialMovesForPiece(piece: Piece, piecesArray: List<Piece>): Piece {
        piece.getSpecialMoves().forEach { specialMovePossible(it, piece, piecesArray) }

        return piece
    }

    fun specialMovePossible(move: MoveType, pieceFrom: Piece, piecesArray: List<Piece>): Piece {
        return when (move) {
            MoveType.SMALL_CASTLE -> {
                val smallCastle = getFieldForCastle(pieceFrom, true, piecesArray)

                return if (smallCastle != null) {
                    pieceFrom.possibleMoves += SpecialMove(MoveType.SMALL_CASTLE, smallCastle)

                    pieceFrom
                } else {
                    pieceFrom
                }
            }

            MoveType.BIG_CASTLE -> {
                val bigCastle = getFieldForCastle(pieceFrom, false, piecesArray)

                return if (bigCastle != null) {
                    pieceFrom.possibleMoves += SpecialMove(MoveType.SMALL_CASTLE, bigCastle)

                    pieceFrom
                } else {
                    pieceFrom
                }
            }

//            MoveType.EN_PASSANT -> {
//                val enPassant = isEnPassantPossible(pieceFrom)
//
//                return if (enPassant != null) {
//                    pieceFrom.specialMoves += SpecialMove(MoveType.EN_PASSANT.name, enPassant)
//                    pieceFrom
//                } else {
//                    pieceFrom
//                }
//            }

            MoveType.MOVE_TWO -> {
                val moveTwo = isMoveTwoPossible(pieceFrom, piecesArray)

                return if (moveTwo != null) {
                    pieceFrom.possibleMoves += SpecialMove(MoveType.MOVE_TWO, moveTwo)
                    pieceFrom
                } else {
                    pieceFrom
                }
            }

            MoveType.PAWN_CAPTURE -> {
                val pawnCapture = isPawnCapturePossible(pieceFrom, piecesArray)

                pawnCapture.forEach { pieceFrom.possibleMoves += SpecialMove(MoveType.PAWN_CAPTURE, it) }
                pieceFrom

            }

            else -> pieceFrom
        }
    }

    fun isPawnCapturePossible(piece: Piece, allPieces: List<Piece>): List<String> {
        val direction = if (piece.color == "white") 1 else -1
        val vector = convertIdToVector(piece.id)
        val leftX: Int = vector.x - 1
        val rightX: Int = vector.x + 1
        val fieldNumber: Int = vector.y + direction
        val leftSideCoordinate: String? = getFieldByXY(leftX, fieldNumber)
        val rightSideCoordinate: String? = getFieldByXY(rightX, fieldNumber)
        val fieldsId = listOfNotNull(leftSideCoordinate, rightSideCoordinate).mapNotNull {
            getPieceById(it, allPieces)
        }
            .filter { it.color != piece.color }

        return fieldsId.map { it.id }
    }

    private fun isMoveTwoPossible(currentPawn: Piece, allPieces: List<Piece>): String? {
        val direction = if (currentPawn.color == "white") 1 else -1
        val firstFieldColumn = currentPawn.id[1] + (direction * 1)
        val secondFieldColumn = currentPawn.id[1] + (direction * 2)
        val firstField = "${currentPawn.id[0]}${firstFieldColumn}"
        val secondField = "${currentPawn.id[0]}${secondFieldColumn}"
        val firstPiece = allPieces.find { it.id == firstField }
        val secondPiece = allPieces.find { it.id == secondField }

        return if (currentPawn.isInStartingPosition() && firstPiece == null && secondPiece == null) {
            secondField
        } else {
            null
        }
    }

//    private fun isEnPassantPossible(pieceFrom: Piece): String? {
//        if (lastMove) {
//            val pawnTwoFieldsMove = lastMove.specialMove === MoveType.MOVE_TWO
//            val lastMoveId = lastMove.fieldTo
//            val direction = if (pieceFrom.color === "white") 1 else -1
//            val isInTheSameLane = pieceFrom.id[1] == lastMoveId[1]
//
//            if (pawnTwoFieldsMove && isInTheSameLane) {
//                val allFields = generateFields()
//                val row = lastMoveId[1] + direction
//
//                return allFields.find { it == "${lastMoveId[0]}${lastMoveId[1] + direction}" }
//            }
//        }
//    }

    private fun getFieldForCastle(pieceFrom: Piece, smallCastle: Boolean, piecesArray: List<Piece>): String? {
        if (getRookToCastle(pieceFrom, smallCastle, piecesArray) != null && checkCastleEmptyFields(
                pieceFrom,
                smallCastle,
                piecesArray
            ) && !pieceFrom.hasMoved
        ) {
            return getReturnedFieldForCastle(pieceFrom, smallCastle)
        }
        return null
    }

    private fun getReturnedFieldForCastle(pieceFrom: Piece, smallCastle: Boolean): String? {
        val returnedFieldX = if (smallCastle) 2 else -2
        val allFields = generateFields()
        val kingLetter = charToNumber(pieceFrom.id[0])
        val column = kingLetter + returnedFieldX
        val letter = numberToChar(column)

        return allFields.find { it == "${letter}${pieceFrom.id[1]}" }
    }

    private fun getRookToCastle(piece: Piece, smallCastle: Boolean, piecesArray: List<Piece>): Piece? {
        val rookX = if (smallCastle) 8 else 1
        val rookField = getPieceById("${piece.id[0]}${rookX}", piecesArray)

        if (rookField is Piece && !rookField.hasMoved) {
            return rookField
        }
        return null
    }

    private fun checkCastleEmptyFields(king: Piece, smallCastle: Boolean, allPieces: List<Piece>): Boolean {
        val size = if (smallCastle) 2 else 3
        val direction = if (smallCastle) 1 else -1
        val row = charToNumber(king.id[0])

        for (i in 1..size) {
            val x = row + (i * direction)
            val kingPositionLetter = numberToChar(x)
            val searchedField = getPieceById("${kingPositionLetter}${king.id[1]}", allPieces)

            if (searchedField != null || checkFieldIsCheckedByEnemy(king.color, searchedField, allPieces)) {
                return false
            }
        }
        return true
    }

    private fun checkFieldIsCheckedByEnemy(color: String, searchedField: Piece?, piecesArray: List<Piece>): Boolean {
        if (searchedField != null) {
            val enemyColor = if (color == "white") "black" else "white"
            val enemyCorrectMoves = getAllPossibleMovesOfPlayer(piecesArray, enemyColor)

            return enemyCorrectMoves.find { piece ->
                piece.possibleMoves.any { it.fieldId == searchedField.id }
            } != null
        }
        return false
    }


    fun getFieldByXY(x: Int, y: Int): String? {
        val column = numberToChar(x)
        val row = y.toString()

        return generateFields().find { field -> field[0] == column && field[1] == row[0] }
    }

    private fun getFieldByID(id: String): String? {

        return generateFields().find { field -> field == id }
    }

    private fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0])
        val y = id[1].digitToInt()

        return Vector2d(x, y)
    }

    fun generateFields(): List<String> {
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

    fun charToNumber(column: Char): Int {
        return column.code - 64
    }

    fun numberToChar(column: Int): Char {
        return (column + 64).toChar()
    }

    fun getPieceById(id: String, allPieces: List<Piece>): Piece? {
        return allPieces.find { it.id == id }
    }

    fun canPreventCheck(allPieces: List<Piece>, color: String): List<String> {
        return allPieces.mapNotNull { piece: Piece ->
            if (piece.canMoveMultipleSquares() && piece.color != color) {
                val direction = piece.getAllPossibleDirectionsWithColor()

                return direction.flatMap { getAllMovesFromDirectionAndSearchKing(piece, it, allPieces) }
            } else {
                null
            }
        }
    }

    private fun getAllMovesFromDirectionAndSearchKing(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        var counter = 1
        var canGoFurther = true
        val vector: Vector2d = convertIdToVector(currentPiece.id)
        val x = direction.x * counter
        val y = direction.y * counter
        var field = getFieldByXY(vector.x + x, vector.y + y)

        if (field != null) {
            var piece = getPieceById(field, allPieces)
            val fields: MutableList<String> = mutableListOf()

            while (field != null && canGoFurther) {
                if (piece!!.isKing() && piece.color != currentPiece.color) {
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
}