package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.GameNotFoundException
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.model.game.Piece
import pl.lpawlowski.chessapp.model.pieces.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.time.LocalDateTime

@Service
class GameService(
    private val gamesRepository: GamesRepository,
) {
    @Transactional
    fun getAllCreatedGames(): List<GameDto> {
        val createdGames = gamesRepository.findGamesByStatus(GameStatus.CREATED.name)

        return createdGames.map { GameDto.fromDomain(it) }
    }

    @Transactional
    fun getUserActiveGame(user: User): GameDto? {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)

        return if (game.isPresent) {
            GameDto.fromDomain(game.get())
        } else {
            null
        }
    }

    @Transactional
    fun resign(user: User) {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)
            .orElseThrow { GameNotFoundException("User does not have an active game!") }

        game.gameStatus = GameStatus.FINISHED.name
    }

    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Long {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds

            if (gameCreateRequest.isWhitePlayer) {
                whitePlayer = user
            } else {
                blackPlayer = user
            }
            pieces = getDefaultChessArrangement()
        }

        val save: Game = gamesRepository.save(game)

        return save.id!!
    }

    @Transactional
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = listOf<Piece>()
        val isCheck = true
        val whoseTurn = "black"
        val lastPlayerMove = LocalDateTime.now()
        val historyOfMoves = ""


        game.moves = when (game.moves.isBlank()) {
            true -> gameMakeMoveRequest.move
            false -> "${game.moves},${gameMakeMoveRequest.move}"
        }
        if (user == game.whitePlayer) {
            game.lastMoveWhite = LocalDateTime.now()
        } else {
            game.lastMoveBlack = LocalDateTime.now()
        }

        return MakeMoveResponse(pieces, isCheck, whoseTurn, lastPlayerMove, historyOfMoves)
    }

    @Transactional
    fun joinGame(user: User, joinGameRequest: JoinGameRequest): GameDto {
        val game = gamesRepository.findById(joinGameRequest.gameId).orElseThrow { RuntimeException("Game not found!") }

        if (game.whitePlayer == null) {
            game.whitePlayer = user
        } else {
            game.blackPlayer = user
        }

        game.lastMoveWhite = LocalDateTime.now()
        game.gameStatus = GameStatus.IN_PROGRESS.name

        return GameDto.fromDomain(game)
    }

    private fun getUserGame(user: User): Game {
        return gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { RuntimeException("Game not found!") }
    }

    private fun getDefaultChessArrangement(): List<pl.lpawlowski.chessapp.model.pieces.Piece> {
        return listOf(
            Pawn("white", "A2", "Pawn"),
            Pawn("white", "B2", "Pawn"),
            Pawn("white", "C2", "Pawn"),
            Pawn("white", "D2", "Pawn"),
            Pawn("white", "E2", "Pawn"),
            Pawn("white", "F2", "Pawn"),
            Pawn("white", "G2", "Pawn"),
            Pawn("white", "H2", "Pawn"),
            Pawn("black", "A7", "Pawn"),
            Pawn("black", "B7", "Pawn"),
            Pawn("black", "C7", "Pawn"),
            Pawn("black", "D7", "Pawn"),
            Pawn("black", "E7", "Pawn"),
            Pawn("black", "F7", "Pawn"),
            Pawn("black", "G7", "Pawn"),
            Pawn("black", "H7", "Pawn"),
            Rook("white", "A1", "Rook"),
            Rook("white", "H1", "Rook"),
            Rook("black", "A8", "Rook"),
            Rook("black", "H8", "Rook"),
            Knight("white", "B1", "Knight"),
            Knight("white", "G1", "Knight"),
            Knight("black", "G8", "Knight"),
            Knight("black", "B8", "Knight"),
            Bishop("white", "C1", "Bishop"),
            Bishop("white", "F1", "Bishop"),
            Bishop("black", "C8", "Bishop"),
            Bishop("black", "F8", "Bishop"),
            King("black", "E8", "King"),
            King("white", "E1", "King"),
            Queen("white", "D1", "Queen"),
            Queen("black", "D8", "Queen"),
        )
    }
}