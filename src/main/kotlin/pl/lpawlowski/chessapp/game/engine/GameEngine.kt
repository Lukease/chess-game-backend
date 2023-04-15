package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.model.game.LastMove
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.web.chess_possible_move.SpecialMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.pieces.*

@Service
class GameEngine(
    private val playerMove: PlayerMove,
) {
    var game: Game? = null
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

    fun getTheKingIsChecked(color: String, piecesArray: List<Piece>): Boolean {
        val king = piecesArray.find { it.color == color && it is King }
        val enemyColor = if (color == "white") "black" else "white"
        val allPossibleMovesOfEnemy = getAllPossibleMovesOfPlayer(piecesArray, enemyColor)

        return checkKingPositionIsChecked(allPossibleMovesOfEnemy, king!!)
    }

    fun checkLastMoveAndReturnPieceArray(moves: String): LastMove {
        var pieceArray = convertFenToPiecesList(getDefaultFen())
        val movesTour = moves.split(",")
        var moveType = MoveType.NORMAL
        var fieldFrom = ""
        var fieldTo = ""
        movesTour.forEachIndexed { index, move: String ->
            val whoseTour = if (index % 2 != 0) "white" else "black"
            val pieceFromLetter = getStartingFile(move)
            val fieldToId = getMoveFieldToId(move)
            val getMovesOfPlayer = getAllPossibleMovesOfPlayer(pieceArray, whoseTour)
            val promotedPieceIcon = getPieceIconIfPawnPromotion(move)
            val promotedPiece = if (promotedPieceIcon != null) createPieceByIcon(promotedPieceIcon, whoseTour) else null
            val piece = pieceArray.firstOrNull { it ->
                it.possibleMoves.any { move ->
                    move.fieldId == fieldToId
                } && (pieceFromLetter == null || it.id[0] == pieceFromLetter[0].uppercaseChar())
            }
            val gameMakeMoveRequest = GameMakeMoveRequest(piece!!.id, fieldToId,
                promotedPiece?.let { PieceDto.fromDomain(it) })
            val makeMove = playerMove.getNameOfMoveAndReturnPieceArray(
                whoseTour,
                gameMakeMoveRequest,
                getMovesOfPlayer
            )
            piece.hasMoved
            pieceArray = makeMove.pieces
            moveType = makeMove.moveType
            fieldFrom = piece.id
            fieldTo = fieldToId
        }

        return LastMove(moveType, fieldFrom, fieldTo, pieceArray)
    }

    fun getStartingFile(move: String): String? {
        val regex = "^[a-h]?"
        return regex.toRegex().find(move)?.value
    }

    fun getPieceIconIfPawnPromotion(move: String): String? {
        val index = move.indexOf("=")
        return if (index >= 0) move.substring(index + 1) else null
    }

    fun getMoveFieldToId(move: String): String {
        val fieldId = "[a-h][1-8]".toRegex()
        val squareMatch = fieldId.find(move)!!.value

        return squareMatch.uppercase()
    }

    fun createPieceByIcon(icon: String, color: String): Piece {
        return when (icon) {
            "♗" -> Bishop(color, "", "Bishop")
            "♔" -> King(color, "", "King")
            "♘" -> Knight(color, "", "Knight")
            "♕" -> Queen(color, "", "Queen")
            "♖" -> Rook(color, "", "Rook")
            else -> Pawn(color, "", "Pawn")
        }
    }

    private fun checkKingPositionIsChecked(allPossibleMovesOfEnemy: List<Piece>, king: Piece): Boolean {
        return allPossibleMovesOfEnemy.find { piece ->
            piece.possibleMoves.any { it.fieldId == king.id }
        } != null
    }


    fun getAllPossibleMovesOfPlayer(piecesArray: List<Piece>, color: String, currentGame: Game? = null): List<Piece> {
        if (currentGame != null) {
            game = currentGame
        }
        val playerPieces: List<Piece> = piecesArray.mapNotNull { piece: Piece ->
            if (piece.color == color) {
                piece
            } else {
                null
            }
        }

        return getAllPossibleMoves(playerPieces, piecesArray)
    }

    fun getEnemyPieces(piecesArray: List<Piece>, color: String): List<Piece> {
        return piecesArray.mapNotNull { piece: Piece ->
            if (piece.color != color) {
                piece
            } else {
                null
            }
        }
    }

    private fun getAllPossibleMoves(playerPieces: List<Piece>, allPieces: List<Piece>): List<Piece> {
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
            piece
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

            MoveType.EN_PASSANT -> {
                val enPassant = isEnPassantPossible(pieceFrom, game!!.moves)

                return if (enPassant != null) {
                    pieceFrom.possibleMoves += SpecialMove(MoveType.EN_PASSANT, enPassant)
                    pieceFrom
                } else {
                    pieceFrom
                }
            }

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

    private fun isEnPassantPossible(pieceFrom: Piece, moves: String): String? =
        checkLastMoveAndReturnPieceArray(moves).takeIf { it.moveType == MoveType.MOVE_TWO }
            ?.let { lastMove ->
                val lastMoveId = lastMove.fieldTo
                val direction = if (pieceFrom.color == "white") 1 else -1
                val isInTheSameLane = pieceFrom.id[1] == lastMoveId[1]
                if (isInTheSameLane) {
                    val allFields = generateFields()
                    val row = lastMoveId[1] + direction
                    allFields.find { it == "${lastMoveId[0]}$row" }
                } else {
                    null
                }
            }

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

    fun dontCauseCheck(allPieces: List<PieceDto>, color: String, piecesArray: List<Piece>): List<PieceDto> {
        val enemyColor = if (color == "white") "black" else "white"
        val king = allPieces.find { it.name == "King" && it.color == color }
        val enemyMoves = getAllPossibleMovesOfPlayer(piecesArray, enemyColor)
        val checkedFields = getCheckedFields(king!!, piecesArray)

        allPieces.forEach { piece ->
            if (piece == king) {
                piece.possibleMoves = piece.possibleMoves.map { move ->
                    val isMoveSafe =
                        enemyMoves.none { enemyPiece -> enemyPiece.possibleMoves.any { it.fieldId == move.fieldId } }
                    if (isMoveSafe) move else null
                }.filterNotNull()
            } else if (checkedFields.isNotEmpty()) {
                piece.possibleMoves = piece.possibleMoves.filter { move ->
                    val isMoveSafe = checkedFields.none { it.contains(move.fieldId) }
                    isMoveSafe
                }
            }
        }

        return allPieces
    }

    private fun getCheckedFields(king: PieceDto, allPieces: List<Piece>): List<List<String>> {
        val checkedFields: MutableList<List<String>> = mutableListOf()

        allPieces.forEach { piece ->
            if (piece.color != king.color) {
                val correctFields: MutableList<String> = mutableListOf()
                piece.getAllPossibleDirectionsWithColor().forEach { direction ->
                    val attackDirectionFields = getAllPossibleMovesFromDirection(piece, direction, allPieces)
                    if (attackDirectionFields.contains(king.id)) {
                        correctFields.addAll(attackDirectionFields)
                    }
                }
                if (piece.isPawn()) {
                    correctFields.addAll(isPawnCapturePossible(piece, allPieces))
                }
                if (correctFields.contains(king.id)) {
                    correctFields.add(piece.id)
                    checkedFields.add(correctFields)
                }
            }
        }

        return checkedFields
    }

    fun checkPieceIsCoveringKing(allPiecesDto: List<PieceDto>, color: String, pieces: List<Piece>): List<PieceDto> {
        val numberOfFieldsWithPiece = 2
        val coveringKingFields = canPreventCheck(pieces, color)
        val numberOfFieldsWhichHavePiece = coveringKingFields.sumOf { fieldId ->
            allPiecesDto.count { it.id == fieldId }
        }
        return allPiecesDto.map { piece ->
            if (coveringKingFields.contains(piece.id) && numberOfFieldsWhichHavePiece == numberOfFieldsWithPiece) {
                piece.possibleMoves = piece.possibleMoves.filter { move ->
                    coveringKingFields.contains(move.fieldId)
                }
            }
            piece
        }
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