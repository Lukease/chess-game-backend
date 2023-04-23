package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.MoveHistory
import pl.lpawlowski.chessapp.web.chess_possible_move.PossibleMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.pieces.*

@Service
class GameEngine(
    private val stringToMoveConverter: StringToMoveConverter,
) {
    fun getDefaultFen(): String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"


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
                    val color = if (char.isUpperCase()) PlayerColor.WHITE else PlayerColor.BLACK
                    val id: String = column + (8 - row)
                    when (char.lowercaseChar()) {
                        'b' -> Bishop(color, id, PiecesNames.BISHOP)
                        'k' -> King(color, id, PiecesNames.KING)
                        'n' -> Knight(color, id, PiecesNames.KNIGHT)
                        'p' -> Pawn(color, id, PiecesNames.PAWN)
                        'q' -> Queen(color, id, PiecesNames.QUEEN)
                        'r' -> Rook(color, id, PiecesNames.ROOK)
                        else -> throw WrongMove("Piece with char: ${char.lowercaseChar()} not found")
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

    fun convertStringToMove(
        pieceFrom: String, pieceToId: String, promotedPiece: String?, pieces: List<Piece>
    ): Move = stringToMoveConverter.createMove(pieceFrom, pieceToId, promotedPiece, pieces)


    fun getTheKingIsChecked(
        color: PlayerColor,
        piecesArray: List<Piece>,
        enemyPieces: List<Piece>,
        moves: String
    ): Boolean {
        val king = piecesArray.find { it.color == color && it is King }
        val allPossibleMovesOfEnemy = enemyPieces.map { getCaptureMoveOfEnemy(it, piecesArray) }

        return checkKingPositionIsChecked(allPossibleMovesOfEnemy, king!!)
    }

    fun checkLastMove(move: String, pieces: List<Piece>, playerColor: PlayerColor): MoveHistory {
        return when (move) {
            "O-O" -> {
                val kingId = if (playerColor == PlayerColor.WHITE) "E1" else "E8"
                val fieldToId = if (playerColor == PlayerColor.WHITE) "G1" else "G8"
                MoveHistory(move, MoveType.SMALL_CASTLE, kingId, fieldToId, null, move.contains("+"))
            }

            "O-O-O" -> {
                val kingId = if (playerColor == PlayerColor.WHITE) "E1" else "E8"
                val fieldToId = if (playerColor == PlayerColor.WHITE) "C1" else "C8"
                MoveHistory(move, MoveType.BIG_CASTLE, kingId, fieldToId, null, move.contains("+"))
            }

            else -> stringToMoveConverter.convertStringMoveToMove(move, pieces, playerColor)
        }
    }

    fun checkKingPositionIsChecked(allPossibleMovesOfEnemy: List<Piece>, king: Piece): Boolean =
        allPossibleMovesOfEnemy.find { piece -> piece.possibleMoves.any { it.fieldId == king.id } } != null


    fun calculateAndReturnAllPossibleMovesOfPlayer(
        piecesArray: List<Piece>,
        color: PlayerColor,
        enemyPieces: List<Piece>,
        lastMove: MoveHistory?
    ): List<Piece> {
        val allPossibleMovesOfPlayer = piecesArray.filter { it.color == color }
            .map { setAllPossibleMovesForPieceAndReturnPiece(it, piecesArray, enemyPieces, lastMove) }
        val correctMoves = dontCauseCheck(allPossibleMovesOfPlayer, color, enemyPieces)

        return checkPieceIsCoveringKingAndFilterMoves(correctMoves, enemyPieces, color)
    }

    fun calculateAndReturnCaptureMoveOfEnemy(piecesArray: List<Piece>, color: PlayerColor): List<Piece> {
        return piecesArray.filter { it.color != color }
            .map { getCaptureMoveOfEnemy(it, piecesArray) }
    }

    fun getEnemyPieces(piecesArray: List<Piece>, color: PlayerColor) = piecesArray.filter { it.color != color }

    fun getCaptureMoveOfEnemy(piece: Piece, allPieces: List<Piece>): Piece {
        return piece.apply {
            possibleMoves = when {
                piece.isKing() -> piece.getAllPossibleDirections()
                    .asSequence()
                    .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                    .flatten()
                    .toList()

                piece.isPawn() -> listOfNotNull(isPawnCapturePossible(piece, allPieces)).flatten()

                else -> {
                    piece.getAllPossibleDirections()
                        .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                        .flatten()
                }
            }
        }
    }


    private fun setAllPossibleMovesForPieceAndReturnPiece(
        piece: Piece,
        playerPieces: List<Piece>,
        enemyPieces: List<Piece>,
        lastMove: MoveHistory?
    ): Piece {
        return piece.apply {
            possibleMoves = when {
                piece.isKing() -> piece.getAllPossibleDirections()
                    .asSequence()
                    .map { direction -> getAllPossibleMovesFromDirection(piece, direction, playerPieces) }
                    .flatten()
                    .plus(getSmallCastleIfPossible(piece, playerPieces, enemyPieces))
                    .plus(getBigCastleIfPossible(piece, playerPieces, enemyPieces))
                    .toList()
                    .filterNotNull()

                piece.isPawn() -> {
                    listOfNotNull(
                        getEnPassantIfPossible(piece, lastMove),
                        getMoveTwoIfPossible(piece, playerPieces),
                        getPossibleMoveOfPawnOneForward(piece, playerPieces)
                    ).plus(isPawnCapturePossible(piece, playerPieces)).map { correctMove ->
                        if ((correctMove.fieldId[1] == '8' && piece.color == PlayerColor.WHITE) || (correctMove.fieldId[1] == '1' && piece.color == PlayerColor.BLACK)) {
                            PossibleMove(MoveType.PROM, correctMove.fieldId)
                        } else {
                            correctMove
                        }
                    }
                }

                else -> {
                    if (piece.id == "B5") {
                        println()
                    }
                    piece.getAllPossibleDirections()
                        .map { direction -> getAllPossibleMovesFromDirection(piece, direction, playerPieces) }
                        .flatten()
                }
            }
        }
    }

    private fun getPossibleMoveOfPawnOneForward(piece: Piece, allPieces: List<Piece>): PossibleMove? {
        val direction = if (piece.color == PlayerColor.WHITE) 1 else -1
        val fieldForward = "${piece.id[0]}${piece.id[1] + direction}"

        return when {
            getFieldById(fieldForward) == null -> null
            allPieces.any { it.id == fieldForward } -> null
            else -> PossibleMove(MoveType.NORMAL, fieldForward)
        }
    }

    private fun getAllPossibleMovesFromDirection(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<PossibleMove> {
        return when {
            currentPiece.canMoveMultipleSquares() -> {
                getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(currentPiece, direction, allPieces)
            }

            else -> {
                listOfNotNull(getPossibleMoveOfKing(currentPiece, direction, allPieces))
            }
        }
    }

    private fun getPossibleMoveOfKing(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): PossibleMove? {
        val vector = convertIdToVector(currentPiece.id)
        val targetField = getFieldByXY(vector.x + direction.x, vector.y + direction.y)
        return targetField?.let {
            if (canMoveToField(it, currentPiece, allPieces)) PossibleMove(MoveType.NORMAL, it) else null
        }
    }

    private fun canMoveToField(field: String, currentPiece: Piece, allPieces: List<Piece>): Boolean {
        val piece = getPieceById(field, allPieces)
        return piece == null || piece.color != currentPiece.color
    }

    private fun getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<PossibleMove> {
        val vector = convertIdToVector(currentPiece.id)
        val fields = mutableListOf<PossibleMove>()
        if (currentPiece.name.name == PiecesNames.KNIGHT.name) {
            println()
        }
        var counter = 1
        var field = getFieldByXY(vector.x + direction.x * counter, vector.y + direction.y * counter)
        while (field != null) {
            val piece = getPieceById(field, allPieces)
            if (piece == null || piece.color != currentPiece.color) {
                fields.add(PossibleMove(MoveType.NORMAL, field))
                if (piece != null) {
                    break
                }
            } else {
                break
            }
            counter++
            field = getFieldByXY(vector.x + direction.x * counter, vector.y + direction.y * counter)
        }
        return fields
    }

    private fun getSmallCastleIfPossible(
        pieceFrom: Piece,
        piecesArray: List<Piece>,
        enemyPieces: List<Piece>
    ): PossibleMove? {
        val smallCastle = getFieldForCastle(pieceFrom, true, piecesArray, enemyPieces)

        return if (smallCastle != null) {
            PossibleMove(MoveType.SMALL_CASTLE, smallCastle)
        } else {
            null
        }
    }

    private fun getBigCastleIfPossible(
        pieceFrom: Piece,
        piecesArray: List<Piece>,
        enemyPieces: List<Piece>
    ): PossibleMove? {
        val bigCastle = getFieldForCastle(pieceFrom, false, piecesArray, enemyPieces)

        return if (bigCastle != null) {
            PossibleMove(MoveType.BIG_CASTLE, bigCastle)
        } else {
            null
        }
    }

    private fun getEnPassantIfPossible(
        pieceFrom: Piece,
        lastMove: MoveHistory?
    ): PossibleMove? {
        return when (lastMove) {
            null -> null
            else -> {
                when (val enPassant = isEnPassantPossible(pieceFrom, lastMove)) {
                    null -> null
                    else -> PossibleMove(MoveType.EN_PASSANT, enPassant)
                }
            }
        }
    }

    private fun getMoveTwoIfPossible(pieceFrom: Piece, piecesArray: List<Piece>): PossibleMove? {
        val moveTwo = isMoveTwoPossible(pieceFrom, piecesArray)
        val conditionForWhitePawn = pieceFrom.color == PlayerColor.WHITE && pieceFrom.id[1] == '2'
        val conditionForBlackPawn = pieceFrom.color == PlayerColor.BLACK && pieceFrom.id[1] == '7'
        return if (moveTwo != null && (conditionForWhitePawn || conditionForBlackPawn)) {
            PossibleMove(MoveType.MOVE_TWO, moveTwo)
        } else {
            null
        }
    }

    private fun isPawnCapturePossible(piece: Piece, allPieces: List<Piece>): List<PossibleMove> {
        val direction = if (piece.color == PlayerColor.WHITE) 1 else -1
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

        return fieldsId.map { PossibleMove(MoveType.PAWN_CAPTURE, it.id) }
    }

    private fun isMoveTwoPossible(currentPawn: Piece, allPieces: List<Piece>): String? {
        val direction = if (currentPawn.color == PlayerColor.WHITE) 1 else -1
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

    private fun isEnPassantPossible(pieceFrom: Piece, lastMove: MoveHistory): String? {
        val direction = if (pieceFrom.color == PlayerColor.WHITE) 1 else -1

        return when (charToNumber(pieceFrom.id[0]) - charToNumber(lastMove.fieldTo[0])) {
            -1, 1 -> {
                val field = "${lastMove.fieldTo[0]}${lastMove.fieldTo[1] + direction}"
                if (field in generateFields()) field else null
            }

            else -> null
        }
    }

    private fun getFieldForCastle(
        pieceFrom: Piece,
        smallCastle: Boolean,
        piecesArray: List<Piece>,
        enemyPieces: List<Piece>
    ): String? {
        if (getRookToCastle(pieceFrom, smallCastle, piecesArray) != null && checkCastleFieldsEmptyAndNotChecked(
                pieceFrom,
                smallCastle,
                piecesArray,
                enemyPieces
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
        val letter = numberToChar(rookX)
        val rookField = getPieceById("${letter}${piece.id[1]}", piecesArray)

        if (rookField is Piece && !rookField.hasMoved) {
            return rookField
        }
        return null
    }

    private fun checkCastleFieldsEmptyAndNotChecked(
        king: Piece,
        smallCastle: Boolean,
        allPieces: List<Piece>,
        enemyPieces: List<Piece>
    ): Boolean {
        val size = if (smallCastle) 2 else 3
        val direction = if (smallCastle) 1 else -1
        val row = charToNumber(king.id[0])

        for (i in 1..size) {
            val x = row + (i * direction)
            val kingPositionLetter = numberToChar(x)
            val searchedPiece = getPieceById("${kingPositionLetter}${king.id[1]}", allPieces)
            val searchedField = getFieldById("${kingPositionLetter}${king.id[1]}")
            val isFieldChecked = checkFieldIsCheckedByEnemy(searchedField, enemyPieces)

            when {
                searchedPiece != null || isFieldChecked -> return false
                i == size && !isFieldChecked -> return true
            }
        }
        return false
    }

    private fun checkFieldIsCheckedByEnemy(
        searchedField: String?,
        enemyPieces: List<Piece>
    ): Boolean {
        if (searchedField != null) {
            val enemyCorrectMoveField = getCheckedFields(enemyPieces).distinct()

            return enemyCorrectMoveField.contains(searchedField)
        }
        return false
    }


    private fun getFieldByXY(x: Int, y: Int): String? {
        if (y < 1 || y > 8) {
            return null
        }
        val column = numberToChar(x)
        val row = y.toString()

        return generateFields().find { field -> field[0] == column && field[1] == row[0] }
    }

    private fun getFieldById(id: String): String? = generateFields().find { field -> field == id }

    private fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0])
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

    private fun charToNumber(column: Char): Int = column.code - 64
    private fun numberToChar(column: Int): Char = (column + 64).toChar()
    private fun getPieceById(id: String, allPieces: List<Piece>): Piece? = allPieces.find { it.id == id }

    fun dontCauseCheck(pieces: List<Piece>, color: PlayerColor, enemyPieces: List<Piece>): List<Piece> {
        val king =
            pieces.find { it.name == PiecesNames.KING && it.color == color }
                ?: throw WrongMove("$color King not found")
        val checkedFields = getCheckedFields(enemyPieces).distinct()

        king.possibleMoves = king.possibleMoves.filterNot { move ->
            checkedFields.contains(move.fieldId)
        }

        return pieces
    }

    private fun getCheckedFields(enemyPieces: List<Piece>): List<String> {
        return enemyPieces.flatMap { piece ->
            piece.possibleMoves.map { it.fieldId }
        }
    }

    fun checkPieceIsCoveringKingAndFilterMoves(
        playerPieces: List<Piece>,
        enemyPieces: List<Piece>,
        color: PlayerColor
    ): List<Piece> {
        val allPieces = listOf(playerPieces, enemyPieces).flatten()

        for (piece in enemyPieces) {
            when {
                piece.canMoveMultipleSquares() -> {
                    val pieceDirections = mutableListOf<String>()
                    val possibleDirections = piece.getAllPossibleDirections()
                    for (direction in possibleDirections) {
                        val fields = getAllMovesFromDirectionAndSearchKing(piece, direction, allPieces).distinct()
                        if (fields.isNotEmpty()) {
                            pieceDirections += fields.plus(piece.id)
                            val numberOfFieldsWhichHavePiece = pieceDirections.count { fieldId ->
                                getPieceById(fieldId, playerPieces) != null
                            }
                            return playerPieces.map { checkedPiece ->
                                if (numberOfFieldsWhichHavePiece == 1 && !checkedPiece.isKing()) {
                                    checkedPiece.possibleMoves =
                                        checkedPiece.possibleMoves.filter { pieceDirections.contains(it.fieldId) }
                                }
                                if (pieceDirections.any { it.contains(checkedPiece.id) } && numberOfFieldsWhichHavePiece == 2 && !checkedPiece.isKing()) {
                                    checkedPiece.possibleMoves =
                                        checkedPiece.possibleMoves.filter { pieceDirections.contains(it.fieldId) }
                                }
                                checkedPiece
                            }
                        }
                    }
                }
            }
        }
        return playerPieces
    }

    private fun getAllMovesFromDirectionAndSearchKing(
        currentPiece: Piece,
        direction: Vector2d,
        allPieces: List<Piece>
    ): List<String> {
        val vector = convertIdToVector(currentPiece.id)
        val fields = mutableListOf<String>()
        var counter = 1
        var field = getFieldByXY(vector.x + direction.x * counter, vector.y + direction.y * counter)
        while (field != null) {
            val piece = getPieceById(field, allPieces)
            if (direction == Vector2d(1, 1) && piece?.id == "B5") {
                println()
            }
            if (piece == null) {
                fields.add(field)
            } else if (piece.color != currentPiece.color) {
                fields.add(field)
                if (piece.isKing()) {
                    return fields
                }
            } else {
                break
            }
            counter++
            field = getFieldByXY(vector.x + direction.x * counter, vector.y + direction.y * counter)
        }
        return emptyList()
    }
}