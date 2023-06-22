package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.model.history.AllHistoryGamesResponse
import pl.lpawlowski.chessapp.model.history.HistoryRequest
import pl.lpawlowski.chessapp.model.history.HistoryResponse
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.pieces.Pawn

@Service
class HistoryService(
    private val gameEngine: GameEngine,
    private val gameService: GameService,
    private val fenConverter: FenConverter,
) {
    fun getAllPlayerGames(user: User): AllHistoryGamesResponse {
        val gamesAsWhite = user.gamesAsWhite.map { GameDto.fromDomain(it) }
        val gamesAsBlack = user.gamesAsBlack.map { GameDto.fromDomain(it) }

        return AllHistoryGamesResponse(gamesAsWhite, gamesAsBlack)
    }

    fun getHistoryFromGame(historyRequest: HistoryRequest, user: User): HistoryResponse {
        val game = when (historyRequest.playAsWhite) {
            true -> user.gamesAsWhite.find { it.id == historyRequest.gameId }
            false -> user.gamesAsBlack.find { it.id == historyRequest.gameId }
        } ?: throw NotFound("Game not found!")

        val moves = game.history.split(",")
        val moveIndex = historyRequest.moveId.toInt()

        when (moveIndex) {
            !in moves.indices -> throw NotFound("Move not found!")
        }

        var fen = gameEngine.getDefaultFen()
        var pieceIdFrom = ""
        var fieldToId = ""

        for (index in 0..moveIndex) {
            val playerColor = when {
                index % 2 == 0 -> PlayerColor.WHITE
                else -> PlayerColor.BLACK
            }
            val enemyColor = when {
                index % 2 != 0 -> PlayerColor.BLACK
                else -> PlayerColor.WHITE
            }

            val lastMoveName = game.historyExtended.split(",")[index]
            val pieces = fenConverter.convertFenToPiecesList(fen)
            val piecesWithMoves = gameEngine.calculateAndReturnCaptureMoveOfEnemy(pieces, enemyColor)
            val lastMove = gameEngine.checkLastMove(lastMoveName, piecesWithMoves, enemyColor, lastMoveName)

            pieceIdFrom = when (lastMoveName.length) {
                5 -> lastMoveName.substring(0, 2).uppercase()
                else -> lastMoveName.substring(1, 3).uppercase()
            }
            fieldToId = lastMoveName.substring(lastMoveName.length - 2).uppercase()

            val piece = when {
                lastMoveName[0].isUpperCase() -> fenConverter.getPieceByChar(
                    lastMoveName[0].lowercaseChar(),
                    playerColor,
                    pieceIdFrom
                ).name

                else -> Pawn(playerColor, pieceIdFrom, PiecesNames.PAWN).name
            }
            val piecesWithCorrectMoves = gameService.getPieceWithCorrectMovesOfPlayer(playerColor, pieces, lastMove)
            val move: Move = gameEngine.convertStringToMove(pieceIdFrom, fieldToId, piece.name, piecesWithCorrectMoves)

            fen = fenConverter.convertPieceListToFen(move.pieces)
        }

        return HistoryResponse(
            fenConverter.convertFenToPiecesList(fen).map { PieceDto.fromDomain(it) },
            listOf(pieceIdFrom, fieldToId),
            GameDto.fromDomain(game)
        )
    }
}