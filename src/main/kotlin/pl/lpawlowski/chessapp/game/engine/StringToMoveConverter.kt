package pl.lpawlowski.chessapp.game.engine

import org.springframework.stereotype.Component
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.MoveHistory
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.web.pieces.*
import java.lang.Math.abs

@Component
class StringToMoveConverter {
    fun createMove(
        pieceFromId: String, fieldToId: String, promotedPieceName: String?, pieces: List<Piece>,
    ): Move {
        val piece = pieces.find { it.id == pieceFromId } ?: throw WrongMove("Piece at id:${pieceFromId} not found")
        val move = piece.possibleMoves.find { it.fieldId == fieldToId }
            ?: throw WrongMove("Possible move to id:${fieldToId} not found")
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

    fun convertStringMoveToMove(
        moveFromTo: String,
        pieces: List<Piece>,
        playerColor: PlayerColor,
        pieceIdFrom: String,
        fieldToId: String,
        piece: Piece,
        historyName: String
    ): MoveHistory {
        val moveType = when (piece.name) {
            PiecesNames.PAWN -> when {
                moveFromTo.contains("=") -> MoveType.PROM
                kotlin.math.abs(pieceIdFrom[1].digitToInt() - fieldToId[1].digitToInt()) == 2 && pieceIdFrom[0] == fieldToId[0] -> MoveType.MOVE_TWO
                kotlin.math.abs(pieceIdFrom[1].digitToInt() - fieldToId[1].digitToInt()) == 1 && pieceIdFrom[0] == fieldToId[0] -> MoveType.NORMAL
                kotlin.math.abs(pieceIdFrom[1].digitToInt() - fieldToId[1].digitToInt()) == 1 && pieceIdFrom[0] != fieldToId[0] && pieces.find { it.id == fieldToId } != null -> MoveType.PAWN_CAPTURE
                kotlin.math.abs(pieceIdFrom[1].digitToInt() - fieldToId[1].digitToInt()) == 1 && pieceIdFrom[0] != fieldToId[0] && pieces.find { it.id == fieldToId } == null -> MoveType.EN_PASSANT
                else -> MoveType.NORMAL
            }
            else -> MoveType.NORMAL
        }
        val promotedPiece = if (moveType == MoveType.PROM) createPieceByIcon(
            moveFromTo.last().toString(),
            playerColor
        ).name.name else null
        return MoveHistory(historyName, moveType, pieceIdFrom, fieldToId, promotedPiece, moveFromTo.contains("+"))
    }

    fun createPieceByIcon(icon: String?, color: PlayerColor): Piece {
        return when (icon) {
            "♗" -> Bishop(color, "A0", PiecesNames.BISHOP)
            "♔" -> King(color, "A0", PiecesNames.KING)
            "♘" -> Knight(color, "A0", PiecesNames.KNIGHT)
            "♕" -> Queen(color, "A0", PiecesNames.QUEEN)
            "♖" -> Rook(color, "A0", PiecesNames.ROOK)
            else -> Pawn(color, "A0", PiecesNames.PAWN)
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
        val fieldFromId = pieceFrom.id
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

        return Move(
            moveName,
            piecesAfterMove,
            MoveType.EN_PASSANT,
            pieceFrom.id,
            fieldTo,
            "",
            getNameOfMoveFromTo(pieceFrom, fieldFromId, fieldTo, "x")
        )
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

        return Move(
            moveName,
            piecesAfterMove,
            moveType,
            fieldFromId,
            fieldTo,
            "",
            getNameOfMoveFromTo(pieceFrom, fieldFromId, fieldTo, "")
        )
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

        return Move(
            moveName,
            piecesAfterMove,
            MoveType.MOVE_TWO,
            fieldFromId,
            fieldTo,
            "",
            getNameOfMoveFromTo(pieceFrom, fieldFromId, fieldTo, "")
        )
    }

    private fun makePromotionMove(
        allPieces: List<Piece>,
        pieceFrom: Piece,
        fieldTo: String,
        captured: String,
        promotedPieceName: String
    ): Move {
        val fieldFromId = pieceFrom.id
        val piecesAfterMove = allPieces.filter { it.id != pieceFrom.id && it.id != fieldTo }
        val promotedPiece = createPiece(promotedPieceName, pieceFrom.color, fieldTo)
        val moveName = captured.plus(fieldTo.lowercase()).plus("=")
            .plus(promotedPiece.getPieceIcon())
        val nameOfMoveFromTo =
            getNameOfMoveFromTo(pieceFrom, fieldFromId, fieldTo, captured).plus("=")
                .plus(promotedPieceName[0].uppercase())

        return Move(
            moveName,
            piecesAfterMove.plus(promotedPiece),
            MoveType.PROM,
            pieceFrom.id,
            fieldTo,
            promotedPieceName,
            nameOfMoveFromTo
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

        return Move(
            moveName,
            piecesAfterMove,
            MoveType.NORMAL,
            fieldFromId,
            fieldTo,
            "",
            getNameOfMoveFromTo(pieceFrom, fieldFromId, fieldTo, capture)
        )
    }

    private fun getNameOfMoveFromTo(pieceFrom: Piece, fieldFromId: String, fieldTo: String, capture: String): String {
        val wasCaptured = if (capture == "x") capture else "-"
        val pieceName = when (pieceFrom.name) {
            PiecesNames.PAWN -> ""
            PiecesNames.KNIGHT -> "N"
            else -> pieceFrom.name.name[0].toString()
        }

        return "$pieceName${fieldFromId.lowercase()}$wasCaptured${fieldTo.lowercase()}"
    }

    fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0])
        val y = id[1].digitToInt()

        return Vector2d(x, y)
    }

    fun charToNumber(column: Char): Int = column.code - 64
    fun numberToChar(column: Int): Char = (column + 64).toChar()
}