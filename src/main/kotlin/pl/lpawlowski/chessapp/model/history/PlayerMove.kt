package pl.lpawlowski.chessapp.model.history

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.web.pieces.Piece
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.Vector2d

@Service
class PlayerMove {
    fun getNameOfMoveAndReturnPieceArray(
        playerColor: String, allPieces: List<PieceDto>, gameMakeMoveRequest: GameMakeMoveRequest, pieces: List<Piece>,
    ): Move {
        val piece = allPieces.find { it.id == gameMakeMoveRequest.pieceFrom }
        val fieldTo = gameMakeMoveRequest.fieldToId
        val move = piece!!.possibleMoves.find { it.fieldId == gameMakeMoveRequest.fieldToId }
        val pieceCaptured = allPieces.find { it.id == gameMakeMoveRequest.fieldToId }
        val pieceFrom = allPieces.find { it.id == gameMakeMoveRequest.pieceFrom }
        val promotedPiece = pieces.find { it.id == gameMakeMoveRequest.pieceFrom }
        val icon = pieces.find { it.id == gameMakeMoveRequest.pieceFrom }!!.getPieceIcon()
        val capture = pieceCaptured?.let { "x" } ?: ""

        return when (move!!.moveType) {
            MoveType.EN_PASSANT -> makeEnPassantMove(allPieces, pieceFrom!!, fieldTo)
            MoveType.SMALL_CASTLE -> makeCastleMove(allPieces, pieceFrom!!, fieldTo, MoveType.SMALL_CASTLE)
            MoveType.BIG_CASTLE -> makeCastleMove(allPieces, pieceFrom!!, fieldTo, MoveType.BIG_CASTLE)
            MoveType.MOVE_TWO -> makeMoveTwo(allPieces, pieceFrom!!, fieldTo)
            MoveType.PROM -> makePromotionMove(allPieces, pieceFrom!!, fieldTo, capture, promotedPiece!!)
            MoveType.PAWN_CAPTURE -> makeNormalMove(allPieces, pieceFrom!!, pieceCaptured!!, fieldTo, icon, capture)
            MoveType.NORMAL -> makeNormalMove(allPieces, pieceFrom!!, pieceCaptured!!, fieldTo, icon, capture)
            else -> {
                Move("", emptyList())
            }
        }
    }

    private fun makeEnPassantMove(
        allPieces: List<PieceDto>,
        pieceFrom: PieceDto,
        fieldTo: String,
    ): Move {
        val direction = if (pieceFrom.color == "white") -1 else 1
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

        return Move(moveName, piecesAfterMove)
    }

    private fun makeCastleMove(
        allPieces: List<PieceDto>,
        pieceFrom: PieceDto,
        fieldTo: String,
        moveType: MoveType
    ): Move {
        val fieldsOldNumber = if (moveType == MoveType.SMALL_CASTLE) 1 else -2
        val fieldsNewNumber = if (moveType == MoveType.SMALL_CASTLE) -1 else 1
        val kingVector = convertIdToVector(pieceFrom.id)
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
        val moveName = moveType.name

        return Move(moveName, piecesAfterMove)
    }

    private fun makeMoveTwo(
        allPieces: List<PieceDto>,
        pieceFrom: PieceDto,
        fieldTo: String,
    ): Move {
        val piecesAfterMove = allPieces.map { piece ->
            if (pieceFrom.id == piece.id) {
                piece.id = fieldTo

                piece
            } else {
                piece
            }
        }
        val moveName = fieldTo.lowercase()

        return Move(moveName, piecesAfterMove)
    }

    private fun makePromotionMove(
        allPieces: List<PieceDto>,
        pieceFrom: PieceDto,
        fieldTo: String,
        captured: String,
        promotedPiece: Piece
    ): Move {
        promotedPiece.id = fieldTo

        val piecesAfterMove = allPieces.filter { it.id != pieceFrom.id }.plus(PieceDto.fromDomain(promotedPiece))
        val moveName = pieceFrom.id[0].lowercase().plus(captured).plus(fieldTo.lowercase()).plus("=")
            .plus(promotedPiece.getPieceIcon())

        return Move(moveName, piecesAfterMove)
    }

    private fun makeNormalMove(
        allPieces: List<PieceDto>,
        pieceFrom: PieceDto,
        pieceCaptured: PieceDto?,
        fieldTo: String,
        icon: String,
        capture: String
    ): Move {
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
        val moveName = icon.plus(capture).plus(fieldTo.lowercase())

        return Move(moveName, piecesAfterMove)
    }

    fun convertIdToVector(id: String): Vector2d {
        val x = charToNumber(id[0])
        val y = id[1].digitToInt()

        return Vector2d(x, y)
    }

    fun charToNumber(column: Char): Int {
        return column.code - 64
    }

    fun numberToChar(column: Int): Char {
        return (column + 64).toChar()
    }
}