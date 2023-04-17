package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.PieceNotFound
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.PossibleMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.pieces.*

@Service
class GameEngine(
    private val stringToMoveConverter: StringToMoveConverter,
) {
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
                    val color = if (char.isUpperCase()) PlayerColor.WHITE else PlayerColor.BLACK
                    val id: String = column + (8 - row)
                    when (char.lowercaseChar()) {
                        'b' -> Bishop(color, id, PiecesNames.BISHOP)
                        'k' -> King(color, id, PiecesNames.KING)
                        'n' -> Knight(color, id, PiecesNames.KNIGHT)
                        'p' -> Pawn(color, id, PiecesNames.PAWN)
                        'q' -> Queen(color, id, PiecesNames.QUEEN)
                        'r' -> Rook(color, id, PiecesNames.ROOK)
                        else -> throw PieceNotFound("Piece with char: ${char.lowercaseChar()} not found")
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
    ): Move {
        return stringToMoveConverter.createMove(pieceFrom, pieceToId, promotedPiece, pieces)
    }

    fun getTheKingIsChecked(color: PlayerColor, piecesArray: List<Piece>, moves: String): Boolean {
        val king = piecesArray.find { it.color == color && it is King }
        val enemyColor = if (color == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
        val allPossibleMovesOfEnemy = calculateAndReturnAllPossibleMovesOfPlayer(piecesArray, enemyColor, moves)

        return checkKingPositionIsChecked(allPossibleMovesOfEnemy, king!!)
    }

    fun checkLastMoveAndReturnPieceArray(moves: String): Move? {
        var pieceArray = convertFenToPiecesList(getDefaultFen())
        val movesTour = moves.split(",")
        var moveType: MoveType?
        var fieldFrom = ""
        var fieldTo = ""
        var lastMove: Move? = null
        movesTour.forEachIndexed { index, move: String ->
//            stringToMoveConverter.convertStringMoveToMove()
        }
        return null
    }

    private fun checkKingPositionIsChecked(allPossibleMovesOfEnemy: List<Piece>, king: Piece): Boolean {
        return allPossibleMovesOfEnemy.find { piece ->
            piece.possibleMoves.any { it.fieldId == king.id }
        } != null
    }


    fun calculateAndReturnAllPossibleMovesOfPlayer(
        piecesArray: List<Piece>,
        color: PlayerColor,
        moves: String
    ): List<Piece> {
        return piecesArray.filter { it.color == color }
            .map { setAllPossibleMovesForPieceAndReturnPiece(it, piecesArray, moves) }
    }

    fun getEnemyPieces(piecesArray: List<Piece>, color: PlayerColor) = piecesArray.filter { it.color != color }


    private fun setAllPossibleMovesForPieceAndReturnPiece(piece: Piece, allPieces: List<Piece>, moves: String): Piece {
        return piece.apply {
            possibleMoves = when {
                piece.isKing() -> piece.getAllPossibleDirections()
                    .asSequence()
                    .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                    .flatten()
                    .plus(getSmallCastleIfPossible(piece, allPieces))
                    .plus(getBigCastleIfPossible(piece, allPieces))
                    .toList()
                    .filterNotNull()

                piece.isPawn() -> {
                    listOfNotNull(
                        getEnPassantIfPossible(piece, allPieces, moves),
                        getMoveTwoIfPossible(piece, allPieces),
                        getPossibleMoveOfPawnOneForward(piece, allPieces)
                    ).plus(isPawnCapturePossible(piece, allPieces))
                }

                else -> {
                    piece.getAllPossibleDirections()
                        .map { direction -> getAllPossibleMovesFromDirection(piece, direction, allPieces) }
                        .flatten()
                }
            }
        }
    }

    private fun getPossibleMoveOfPawnOneForward(piece: Piece, allPieces: List<Piece>): PossibleMove? {
        val direction = if (piece.color == PlayerColor.WHITE) 1 else -1
        val fieldForward = "${piece.id[0]}${piece.id[1] + direction}"

        return when {
            getFieldByID(fieldForward) == null -> null
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
        if (currentPiece.name.name == PiecesNames.KNIGHT.name){
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

    fun getSmallCastleIfPossible(pieceFrom: Piece, piecesArray: List<Piece>): PossibleMove? {
        val smallCastle = getFieldForCastle(pieceFrom, true, piecesArray)

        return if (smallCastle != null) {
            PossibleMove(MoveType.SMALL_CASTLE, smallCastle)
        } else {
            null
        }
    }

    fun getBigCastleIfPossible(pieceFrom: Piece, piecesArray: List<Piece>): PossibleMove? {
        val bigCastle = getFieldForCastle(pieceFrom, false, piecesArray)

        return if (bigCastle != null) {
            PossibleMove(MoveType.BIG_CASTLE, bigCastle)
        } else {
            null
        }
    }

    fun getEnPassantIfPossible(pieceFrom: Piece, piecesArray: List<Piece>, moves: String): PossibleMove? {
        val enPassant = isEnPassantPossible(pieceFrom, moves)

        return if (enPassant != null) {
//            PossibleMove(MoveType.EN_PASSANT, enPassant)
            return null
        } else {
            null
        }
    }

    fun getMoveTwoIfPossible(pieceFrom: Piece, piecesArray: List<Piece>): PossibleMove? {
        val moveTwo = isMoveTwoPossible(pieceFrom, piecesArray)
        return if (moveTwo != null && (pieceFrom.id[1] == '2' || pieceFrom.id[1] == '7')) {
            PossibleMove(MoveType.MOVE_TWO, moveTwo)
        } else {
            null
        }
    }

    fun isPawnCapturePossible(piece: Piece, allPieces: List<Piece>): List<PossibleMove> {
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

    private fun isEnPassantPossible(pieceFrom: Piece, moves: String): String? {
        val lastMove = checkLastMoveAndReturnPieceArray(moves)
        if (lastMove?.moveType != MoveType.MOVE_TWO) {
            return null
        }
        val lastMoveId = lastMove.fieldTo
        val direction = if (pieceFrom.color == PlayerColor.WHITE) 1 else -1

        if (pieceFrom.id[1] != lastMoveId[1]) {
            return null
        }
        val allFields = generateFields()
        val row = lastMoveId[1] + direction
        val field = "${lastMoveId[0]}$row"
        return if (allFields.find { it == field } != null) {
            field
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
        val letter = numberToChar(rookX)
        val rookField = getPieceById("${letter}${piece.id[1]}", piecesArray)

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

    private fun checkFieldIsCheckedByEnemy(
        color: PlayerColor,
        searchedField: Piece?,
        piecesArray: List<Piece>
    ): Boolean {
        if (searchedField != null) {
//            val enemyColor = if (color == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
//            val enemyCorrectMoves = calculateAndReturnAllPossibleMovesOfPlayer(piecesArray, enemyColor, moves )
//
//            return enemyCorrectMoves.find { piece ->
//                piece.possibleMoves.any { it.fieldId == searchedField.id }
//            } != null
        }
        return false
    }


    fun getFieldByXY(x: Int, y: Int): String? {
        if (y < 1 || y > 8) {
            return null
        }
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

//    fun dontCauseCheck(allPieces: List<PieceDto>, color: PlayerColor, piecesArray: List<Piece>): List<PieceDto> {
//        val enemyColor = if (color == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
//        val king = allPieces.find { it.name == PiecesNames.KING && it.color == color }
//        val enemyMoves = calculateAndReturnAllPossibleMovesOfPlayer(piecesArray, enemyColor)
//        val checkedFields = getCheckedFields(king!!, piecesArray)
//
//        allPieces.forEach { piece ->
//            if (piece == king) {
//                piece.possibleMoves = piece.possibleMoves.map { move ->
//                    val isMoveSafe =
//                        enemyMoves.none { enemyPiece -> enemyPiece.possibleMoves.any { it.fieldId == move.fieldId } }
//                    if (isMoveSafe) move else null
//                }.filterNotNull()
//            } else if (checkedFields.isNotEmpty()) {
//                piece.possibleMoves = piece.possibleMoves.filter { move ->
//                    val isMoveSafe = checkedFields.none { it.contains(move.fieldId) }
//                    isMoveSafe
//                }
//            }
//        }
//
//        return allPieces
//    }

//    private fun getCheckedFields(king: Piece, allPieces: List<Piece>): List<List<String>> {
//        val checkedFields: MutableList<List<String>> = mutableListOf()
//
//        allPieces.forEach { piece ->
//            if (piece.color != king.color) {
//                val correctFields: MutableList<String> = mutableListOf()
//                piece.getAllPossibleDirections().forEach { direction ->
//                    val attackDirectionFields = getAllPossibleMovesFromDirection(piece, direction, allPieces)
//                    if (attackDirectionFields.contains(king.id)) {
//                        correctFields.addAll(attackDirectionFields)
//                    }
//                }
//                if (piece.isPawn()) {
//                    correctFields.addAll(isPawnCapturePossible(piece, allPieces))
//                }
//                if (correctFields.contains(king.id)) {
//                    correctFields.add(piece.id)
//                    checkedFields.add(correctFields)
//                }
//            }
//        }
//
//        return checkedFields
//    }

    fun checkPieceIsCoveringKing(
        allPiecesDto: List<PieceDto>,
        color: PlayerColor,
        pieces: List<Piece>
    ): List<PieceDto> {
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

    fun canPreventCheck(allPieces: List<Piece>, color: PlayerColor): List<String> {
        return allPieces.mapNotNull { piece: Piece ->
            if (piece.canMoveMultipleSquares() && piece.color != color) {
                val direction = piece.getAllPossibleDirections()

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