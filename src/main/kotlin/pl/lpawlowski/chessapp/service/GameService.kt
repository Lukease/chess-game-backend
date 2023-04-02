package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.model.pieces.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.game.engine.GameEngine
import java.time.LocalDateTime

@Service
class GameService(
    private val gamesRepository: GamesRepository,
    private val gameEngine: GameEngine
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
    fun getUserActiveGameAndReturnMoves(user: User): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = gameEngine.convertFenToPiecesList(game.fen)
        val color = if (user.login == game.whitePlayer?.login) "white" else "black"
        val piecesWithCorrectMoves = gameEngine.getAllPossibleMovesOfPlayer(pieces, color)
        val moves = game.moves.split(",")
        val whoseTurn = if (moves.size % 2 == 0) "white" else "black"

        return MakeMoveResponse(piecesWithCorrectMoves, GameDto.fromDomain(game), whoseTurn)
    }

    @Transactional
    fun resign(user: User) {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)
            .orElseThrow { NotFound("User does not have an active game!") }

        game.gameStatus = GameStatus.FINISHED.name
    }

    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Long {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds
            fen = gameEngine.getDefaultFen()

            if (gameCreateRequest.isWhitePlayer) {
                whitePlayer = user
            } else {
                blackPlayer = user
            }
        }

        val save: Game = gamesRepository.save(game)

        return save.id!!
    }

    @Transactional
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = listOf<Piece>()
        val moves = game.moves.split(",")
        val whoseTurn = if (moves.size % 2 == 0) "white" else "black"

        game.moves = when (game.moves.isBlank()) {
            true -> gameMakeMoveRequest.move
            false -> "${game.moves},${gameMakeMoveRequest.move}"
        }
        if (user == game.whitePlayer) {
            game.lastMoveWhite = LocalDateTime.now()
        } else {
            game.lastMoveBlack = LocalDateTime.now()
        }
        val pieceDto = pieces.map { PieceDto.fromDomain(it) }

        return MakeMoveResponse(pieceDto, GameDto.fromDomain(game), whoseTurn)
    }

    @Transactional
    fun joinGame(user: User, joinGameRequest: JoinGameRequest): JoinGameResponse {
        val game = gamesRepository.findById(joinGameRequest.gameId).orElseThrow { RuntimeException("Game not found!") }

        if (game.whitePlayer == null) {
            game.whitePlayer = user
        } else {
            game.blackPlayer = user
        }

        game.lastMoveWhite = LocalDateTime.now()
        game.gameStatus = GameStatus.IN_PROGRESS.name

        return JoinGameResponse(game.id!!)

    }

    private fun getUserGame(user: User): Game {
        return gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { RuntimeException("Game not found!") }
    }
}