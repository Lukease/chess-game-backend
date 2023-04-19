package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Component
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.pieces.*

@Component
class StringToMoveConverter {
    fun createMove(
        pieceFromId: String, fieldToId: String, promotedPieceName: String?, pieces: List<Piece>,
    ): Move {
        val piece = pieces.find { it.id == pieceFromId } ?: throw WrongMove("Piece at id:${pieceFromId} not found")
        val move = piece.possibleMoves.find { it.fieldId == fieldToId }
            ?: throw WrongMove("Possible move to id:${pieceFromId} not found")
        val pieceCaptured = pieces.find { it.id == fieldToId }
        val capture = pieceCaptured?.let { "x" } ?: ""

        return when (move.moveType) {
            MoveType.EN_PASSANT -> makeEnPassantMove(pieces, piece, fieldToId)
            MoveType.SMALL_CASTLE -> makeCastleMove(pieces, piece, fieldToId, MoveType.SMALL_CASTLE)
            MoveType.BIG_CASTLE -> makeCastleMove(pieces, piece, fieldToId, MoveType.BIG_CASTLE)
            MoveType.MOVE_TWO -> makeMoveTwo(pieces, piece, fieldToId)
            MoveType.PROM -> makePromotionMove(pieces, piece, fieldToId, capture, promotedPieceName!!)
            MoveType.PAWN_CAPTURE -> makeNormalMove(pieces, piece, pieceCaptured, fieldToId, capture)
            MoveType.NORMAL -> makeNormalMove(pieces, piece, pieceCaptured, fieldToId, capture)
        }
    }

    //    fun convertStringMoveToMove(move: String, pieces: List<Piece>,index: Int): List<Piece>{
//        val whoseTour = if (index % 2 != 0) PlayerColor.WHITE else PlayerColor.BLACK
//        val pieceFromLetter = getStartingFile(move)
//        val fieldToId = getMoveFieldToId(move)
//
////            val getMovesOfPlayer = calculateAndReturnAllPossibleMovesOfPlayer(pieceArray, whoseTour, moves)
//        val promotedPieceIcon = getPieceIconIfPawnPromotion(move)
//        val promotedPiece =
//            if (promotedPieceIcon != null) createPieceByIcon(promotedPieceIcon, whoseTour) else null
//        val piece = pieceArray.first { it ->
//            it.possibleMoves.any { move ->
//                move.fieldId == fieldToId
//            } && (pieceFromLetter == null || it.id[0] == pieceFromLetter[0].uppercaseChar())
//        }
//    }
    fun getStartingFile(move: String): String? {
        return if (move.length > 2) {
            val regex = "^[a-h]?"
            regex.toRegex().find(move)?.value
        } else {
            null
        }
    }

    fun getMoveFieldToId(move: String): String {
        val fieldId = "[a-h][1-8]".toRegex()
        val squareMatch = fieldId.find(move)!!.value

        return squareMatch.uppercase()
    }

    fun createPieceByIcon(icon: String, color: PlayerColor): Piece {
        return when (icon) {
            "♗" -> Bishop(color, "", PiecesNames.BISHOP)
            "♔" -> King(color, "", PiecesNames.KING)
            "♘" -> Knight(color, "", PiecesNames.KNIGHT)
            "♕" -> Queen(color, "", PiecesNames.QUEEN)
            "♖" -> Rook(color, "", PiecesNames.ROOK)
            else -> Pawn(color, "", PiecesNames.PAWN)
        }
    }

    fun createPiece(name: String, color: PlayerColor, id: String): Piece {
        return when (name) {
            "Bishop" -> Bishop(color, id, PiecesNames.BISHOP)
            "King" -> King(color, id, PiecesNames.KING)
            "Knight" -> Knight(color, id, PiecesNames.KNIGHT)
            "Pawn" -> Pawn(color, id, PiecesNames.PAWN)
            "Queen" -> Queen(color, id, PiecesNames.QUEEN)
            "Rook" -> Rook(color, id, PiecesNames.ROOK)
            else -> throw WrongMove("Invalid piece name: $name")
        }
    }


    private fun makeEnPassantMove(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        fieldTo: String,
    ): Move {
        val direction = if (pieceFrom.color == PlayerColor.WHITE) -1 else 1
        val capturedPieceRow = fieldTo[1] + direction
        val capturedPieceId = "${fieldTo[0]}$capturedPieceRow"
        val piecesAfterMove = allPieces.filter { it.id != capturedPieceId }.map { piece ->
            if (pieceFrom.id == piece.id) {
                piece.id = fieldTo

                piece
            } else {
                piece
            }
        }
        val moveName = pieceFrom.id[0].lowercase().plus("x").plus(fieldTo.lowercase())

        return Move(moveName, piecesAfterMove, MoveType.EN_PASSANT, pieceFrom.id, fieldTo, "")
    }

    private fun makeCastleMove(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        fieldTo: String,
        moveType: MoveType
    ): Move {
        val fieldsOldNumber = if (moveType == MoveType.SMALL_CASTLE) 1 else -2
        val fieldsNewNumber = if (moveType == MoveType.SMALL_CASTLE) -1 else 1
        val fieldFromId = pieceFrom.id
        val kingVector = convertIdToVector(fieldTo)
        val oldRookFieldId = "${numberToChar(kingVector.x + fieldsOldNumber)}${kingVector.y}"
        val newRookFieldId = "${numberToChar(kingVector.x + fieldsNewNumber)}${kingVector.y}"
        val piecesAfterMove = allPieces.map { piece ->
            if (pieceFrom.id == piece.id) {
                piece.id = fieldTo
                piece
            } else if (piece.id == oldRookFieldId) {
                piece.id = newRookFieldId
                piece
            } else {
                piece
            }
        }
        val moveName = moveType.historyNotation

        return Move(moveName, piecesAfterMove, moveType, fieldFromId, fieldTo, "")
    }

    private fun makeMoveTwo(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        fieldTo: String,
    ): Move {
        val fieldFromId = pieceFrom.id
        val piecesAfterMove = allPieces.map { piece ->
            if (pieceFrom.id == piece.id) {
                piece.id = fieldTo

                piece
            } else {
                piece
            }
        }
        val moveName = fieldTo.lowercase()

        return Move(moveName, piecesAfterMove, MoveType.MOVE_TWO, fieldFromId, fieldTo, "")
    }

    private fun makePromotionMove(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        fieldTo: String,
        captured: String,
        promotedPieceName: String
    ): Move {
        val piecesAfterMove = allPieces.filter { it.id != pieceFrom.id && it.id != fieldTo }
        val promotedPiece = createPiece(promotedPieceName, pieceFrom.color, fieldTo)
        val moveName = captured.plus(fieldTo.lowercase()).plus("=")
            .plus(promotedPiece.getPieceIcon())

        return Move(
            moveName,
            piecesAfterMove.plus(promotedPiece),
            MoveType.PROM,
            pieceFrom.id,
            fieldTo,
            promotedPieceName
        )
    }

    private fun makeNormalMove(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        pieceCaptured: Piece?,
        fieldTo: String,
        capture: String
    ): Move {
        val fieldFromId = pieceFrom.id
        if (pieceCaptured != null) {
            allPieces.filter { it.id != pieceCaptured.id }
        }
        val piecesAfterMove = allPieces.map { piece ->
            if (pieceFrom.id == piece.id) {
                piece.id = fieldTo

                piece
            } else {
                piece
            }
        }
        val moveName = pieceFrom.getPieceIcon().plus(capture).plus(fieldTo.lowercase())

        return Move(moveName, piecesAfterMove, MoveType.NORMAL, fieldFromId, fieldTo, "")
    }

    fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0])
        val y = id[1].digitToInt()

        return Vector2d(x, y)
    }

    fun charToNumber(column: Char): Int = column.code - 64
    fun numberToChar(column: Int): Char = (column + 64).toChar()
}