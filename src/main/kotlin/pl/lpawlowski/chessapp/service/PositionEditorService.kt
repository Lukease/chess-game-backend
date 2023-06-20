package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.EditForbidden
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.model.positionEditor.ChangePositionOfPieceInPositionEditor
import pl.lpawlowski.chessapp.model.positionEditor.PositionEditorResponse
import pl.lpawlowski.chessapp.repositories.UsersRepository
import pl.lpawlowski.chessapp.web.pieces.Piece

@Service
class PositionEditorService(
    private val gameEngine: GameEngine,
    private val fenConverter: FenConverter,
    private val usersRepository: UsersRepository,
) {
    fun getPositionEditorPieces(user: User): PositionEditorResponse {
        val fen = user.positionEditorFen
        val pieces = fenConverter.convertFenToPiecesList(fen)

        return returnPiecesWithMovesForPositionEditor(pieces)
    }

    @Transactional
    fun getDefaultPiecesToPositionEditor(user: User): PositionEditorResponse {
        val fen = gameEngine.getDefaultFen()

        user.positionEditorFen = fen

        val pieces = fenConverter.convertFenToPiecesList(fen)

        return returnPiecesWithMovesForPositionEditor(pieces)
    }

    @Transactional
    fun removePieceFromPositionEditor(pieceId: String, user: User): PositionEditorResponse {
        val fen = user.positionEditorFen
        val pieces = fenConverter.convertFenToPiecesList(fen)
        val piece = pieces.find { it.id == pieceId }
        when {
            piece != null && piece.isKing() -> throw EditForbidden("You can't remove the King!")
        }
        val filteredPieces = pieces.filter { it.id != pieceId }
        val filteredFen = fenConverter.convertPieceListToFen(filteredPieces)

        user.positionEditorFen = filteredFen
        usersRepository.save(user)

        return returnPiecesWithMovesForPositionEditor(filteredPieces)
    }

    @Transactional
    fun changePositionOfPiece(
        newPositionRequest: ChangePositionOfPieceInPositionEditor,
        user: User
    ): PositionEditorResponse {
        val fen = user.positionEditorFen
        val pieces = fenConverter.convertFenToPiecesList(fen)

        when {
            pieces.any { it.id == newPositionRequest.newId } -> {
                throw EditForbidden("Can't add a piece to this field!")
            }

            newPositionRequest.isFromBoard -> {
                val piecesWithChangedPosition = pieces.map { piece ->
                    when (piece.id) {
                        newPositionRequest.piece.id -> {
                            piece.id = newPositionRequest.newId
                            piece
                        }

                        else -> piece
                    }
                }

                val newFen = fenConverter.convertPieceListToFen(piecesWithChangedPosition)

                user.positionEditorFen = newFen
                usersRepository.save(user)

                return returnPiecesWithMovesForPositionEditor(piecesWithChangedPosition)
            }

            else -> {
                val newPiece = PieceDto.toDomain(newPositionRequest.piece).apply {
                    id = newPositionRequest.newId
                }
                val updatedPieces = pieces.plus(newPiece)

                val newFen = fenConverter.convertPieceListToFen(updatedPieces)

                user.positionEditorFen = newFen
                usersRepository.save(user)

                return returnPiecesWithMovesForPositionEditor(updatedPieces)
            }
        }
    }

    @Transactional
    fun setOwnFen(fen: String, user: User): PositionEditorResponse {
        if (!isValidFen(fen)) {
            throw IllegalArgumentException("Invalid FEN format!")
        }

        user.positionEditorFen = fen

        val pieces = fenConverter.convertFenToPiecesList(fen)

        return returnPiecesWithMovesForPositionEditor(pieces)
    }

    private fun isValidFen(fen: String): Boolean {
        val ranks = fen.split('/')
        if (ranks.size != 8) {
            return false
        }

        for (rank in ranks) {
            if (rank.length > 8 || !rank.matches(Regex("[1-8knpqbrKQRBNP]{1,8}"))) {
                return false
            }
        }

        return true
    }


    private fun returnPiecesWithMovesForPositionEditor(pieces: List<Piece>): PositionEditorResponse {
        val whitePieces = pieces.filter { it.color == PlayerColor.WHITE }
        val blackPieces = pieces.filter { it.color == PlayerColor.BLACK }
        val whitePiecesWithMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(pieces, PlayerColor.WHITE, blackPieces, null)
        val blackPiecesWithMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(pieces, PlayerColor.BLACK, whitePieces, null)
        val checkedKingsId = getCheckedKingsId(blackPiecesWithMoves, whitePiecesWithMoves)

        return PositionEditorResponse(
            whitePiecesWithMoves.map { PieceDto.fromDomain(it) }
                .plus(blackPiecesWithMoves.map { PieceDto.fromDomain(it) }),
            checkedKingsId
        )
    }

    private fun getCheckedKingsId(
        enemyPieces: List<Piece>,
        playerPieces: List<Piece>
    ): List<String> {
        val whitePlayerKing = findKing(playerPieces, PlayerColor.WHITE)
        val blackPlayerKing = findKing(enemyPieces, PlayerColor.BLACK)
        val isWhiteKingChecked =
            if (gameEngine.checkKingPositionIsChecked(enemyPieces, whitePlayerKing)) whitePlayerKing.id else null
        val isBlackKingChecked =
            if (gameEngine.checkKingPositionIsChecked(playerPieces, blackPlayerKing)) blackPlayerKing.id else null

        return listOfNotNull(isBlackKingChecked, isWhiteKingChecked)
    }

    private fun findKing(pieces: List<Piece>, color: PlayerColor): Piece {
        return pieces.find { it.color == color && it.name == PiecesNames.KING }
            ?: throw NotFound("$color King not found!!")
    }
}