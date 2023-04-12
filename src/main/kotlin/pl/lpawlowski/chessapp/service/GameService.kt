package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.model.game.PieceDto
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
        val moves = game.moves.split(",")
        val whoseTurn = if (moves.size % 2 != 0) "white" else "black"
        val color = if (user.login == game.whitePlayer?.login) "white" else "black"

        val piecesWithCorrectMoves = if (whoseTurn == color) {
            val playerPieceDto = gameEngine.getAllPossibleMovesOfPlayer(pieces, color)
            val enemyPieces = gameEngine.getEnemyPieces(pieces, color)

            playerPieceDto.plus(enemyPieces)
        } else {
            pieces.map { PieceDto.fromDomain(it) }
        }

        val kingIsChecked = gameEngine.getTheKingIsChecked(color, pieces)

        return MakeMoveResponse(piecesWithCorrectMoves, GameDto.fromDomain(game), whoseTurn, color, kingIsChecked)
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
        val pieces = gameEngine.convertFenToPiecesList(game.fen)
        val moves = game.moves.split(",")
        val whoseTurn = if (moves.size % 2 != 0) "white" else "black"
        val playerColor = if (user == game.whitePlayer) "white" else "black"
        val removePieceToIfExist = pieces.filter { it.id != gameMakeMoveRequest.moveId }.map { piece ->
            if (gameMakeMoveRequest.piece.id == piece.id) {
                if (gameMakeMoveRequest.moveName == "") {
                    piece.id = gameMakeMoveRequest.moveId

                    piece
                } else {
                    piece
                }
            } else {
                piece
            }
        }

        game.fen = gameEngine.convertPieceListToFen(removePieceToIfExist)

        game.moves = when (game.moves.isBlank()) {
            true -> gameMakeMoveRequest.moveId
            false -> "${game.moves},${gameMakeMoveRequest.moveId}"
        }
        if (user == game.whitePlayer) {
            game.lastMoveWhite = LocalDateTime.now()
        } else {
            game.lastMoveBlack = LocalDateTime.now()
        }
        val pieceDto = removePieceToIfExist.map { PieceDto.fromDomain(it) }
        val kingIsChecked = gameEngine.getTheKingIsChecked(playerColor, pieces)

        return MakeMoveResponse(pieceDto, GameDto.fromDomain(game), whoseTurn, playerColor, kingIsChecked)
    }

    @Transactional
    fun joinGame(user: User, joinGameRequest: JoinGameRequest): JoinGameResponse {
        val game = gamesRepository.findById(joinGameRequest.gameId).orElseThrow { RuntimeException("Game not found!") }
        val userActiveGame = gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
        if (game.whitePlayer?.login != user.login && game.blackPlayer?.login != user.login && !userActiveGame.isPresent) {
            if (game.whitePlayer == null) {
                game.whitePlayer = user
            } else {
                game.blackPlayer = user
            }

            game.lastMoveWhite = LocalDateTime.now()
            game.gameStatus = GameStatus.IN_PROGRESS.name

            return JoinGameResponse(game.id!!)
        } else {
            throw RuntimeException("You are already in the game!")
        }
    }

    private fun getUserGame(user: User): Game {
        return gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { RuntimeException("Game not found!") }
    }
}