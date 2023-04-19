package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.ForbiddenUser
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.pieces.Piece
import java.time.LocalDateTime

@Service
class GameService(
    private val gamesRepository: GamesRepository,
    private val gameEngine: GameEngine,
    private val drawOffersRepository: DrawOffersRepository
) {
    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Game {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds
            fen = gameEngine.getDefaultFen()

            when (gameCreateRequest.isWhitePlayer) {
                true -> whitePlayer = user
                false -> blackPlayer = user
            }
        }

        return gamesRepository.save(game)
    }

    @Transactional
    fun getAllCreatedGames(): List<Game> {
        return gamesRepository.findGamesByStatus(GameStatus.CREATED.name)
    }

    @Transactional
    fun joinGame(user: User, joinGameRequest: JoinGameRequest): JoinGameResponse {
        val game = gamesRepository.findById(joinGameRequest.gameId).orElseThrow { RuntimeException("Game not found!") }
        val userActiveGame = gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
        when {
            game.whitePlayer?.login == user.login || game.blackPlayer?.login == user.login -> throw RuntimeException("You are already in the game!")
            userActiveGame.isPresent -> throw RuntimeException("You are already in an active game!")
            game.whitePlayer == null -> game.whitePlayer = user
            else -> game.blackPlayer = user
        }
        game.lastMoveWhite = LocalDateTime.now()
        game.gameStatus = GameStatus.IN_PROGRESS.name

        return JoinGameResponse(game.id!!)
    }

    @Transactional
    fun getUserActiveGame(user: User): Game? {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)

        return when {
            game.isPresent -> game.get()
            else -> null
        }
    }

    @Transactional
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = gameEngine.convertFenToPiecesList(game.fen)
        val moves = game.moves.split(",")
        val whoseTurn = when {
            moves.size % 2 != 0 -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val playerColor = when (user) {
            game.whitePlayer -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }

        when (playerColor) {
            whoseTurn -> {
                val piecesWithCorrectMoves = getPieceWithCorrectMovesOfPlayer(playerColor, pieces, game.moves)
                val move: Move = gameEngine.convertStringToMove(
                    gameMakeMoveRequest.pieceFromId,
                    gameMakeMoveRequest.fieldToId,
                    gameMakeMoveRequest.promotedPieceName,
                    piecesWithCorrectMoves
                )

                game.fen = gameEngine.convertPieceListToFen(move.pieces)

                val pieceDto = move.pieces.map { PieceDto.fromDomain(it) }
                val kingIsChecked = gameEngine.getTheKingIsChecked(playerColor, pieces, game.moves)
                val isCheck = if (kingIsChecked) "+" else ""
                game.moves = when (game.moves.isBlank()) {
                    true -> move.nameOfMove.plus(isCheck)
                    false -> "${game.moves},${move.nameOfMove.plus(isCheck)}"
                }
                when (user) {
                    game.whitePlayer -> game.lastMoveWhite = LocalDateTime.now()
                    else -> game.lastMoveBlack = LocalDateTime.now()
                }

                return MakeMoveResponse(
                    pieceDto,
                    GameDto.fromDomain(game),
                    whoseTurn.name,
                    playerColor.name,
                    kingIsChecked
                )
            }

            else -> throw ForbiddenUser("It's not your turn!")
        }
    }

    @Transactional
    fun getUserActiveGameAndReturnMoves(user: User): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = gameEngine.convertFenToPiecesList(game.fen)
        val moves = game.moves.split(",")
        val whoseTurn = when {
            moves.size % 2 != 0 -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val playerColor = when (user) {
            game.whitePlayer -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val piecesWithCorrectMoves =
            if (playerColor == whoseTurn) getPieceWithCorrectMovesOfPlayer(playerColor, pieces, game.moves) else pieces
        val kingIsChecked = gameEngine.getTheKingIsChecked(playerColor, pieces, game.moves)
        //todo king is checked add possible moves need to remove it
        return MakeMoveResponse(
            piecesWithCorrectMoves.map { PieceDto.fromDomain(it) },
            GameDto.fromDomain(game),
            whoseTurn.name.lowercase(),
            playerColor.name.lowercase(),
            kingIsChecked
        )
    }

    @Transactional
    fun resign(user: User): Game {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)
            .orElseThrow { NotFound("User does not have an active game!") }

        game.gameStatus = GameStatus.FINISHED.name

        return game
    }

    @Transactional
    fun createOffer(user: User): Long? {
        val gameOffer = gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { NotFound("No active game!!") }

        return when {
            gameOffer != null -> {
                val drawOffers = DrawOffers().apply {
                    game = gameOffer
                    status = DrawOffersStatus.OFFERED.name
                    playerOffered = user
                    playerResponding = if (gameOffer.whitePlayer?.login == user.login) {
                        gameOffer.blackPlayer!!
                    } else {
                        gameOffer.whitePlayer!!
                    }
                }

                drawOffersRepository.save(drawOffers).id!!
            }

            else -> throw NotFound("No active game!!")
        }
    }

    @Transactional
    fun responseOffer(user: User, gameDrawOfferRequest: GameDrawOfferRequest): Long {
        val drawOffer = drawOffersRepository.findById(gameDrawOfferRequest.gameOfferId)
            .orElseThrow { NotFound("Draw offer not found!") }

        return when {
            drawOffer.playerResponding.login != user.login -> {
                throw ForbiddenUser("You are not the player asked to respond to the draw offer!")
            }

            gameDrawOfferRequest.playerResponse -> {
                drawOffer.game.gameStatus = GameStatus.FINISHED.name
                drawOffer.game.result = GameResult.DRAW.name
                drawOffer.status = DrawOffersStatus.ACCEPTED.name
                drawOffer.id!!
            }

            else -> {
                drawOffer.status = DrawOffersStatus.REJECTED.name
                drawOffer.id!!
            }
        }
    }

    fun getDrawOffer(user: User): DrawOffers =
        drawOffersRepository.findByUserAndStatus(user, DrawOffersStatus.OFFERED.name)
            .orElseThrow { NotFound("Draw offer not found!") }


    private fun getUserGame(user: User): Game {
        return gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { RuntimeException("Active game not found!") }
    }

    private fun getPieceWithCorrectMovesOfPlayer(
        playerColor: PlayerColor,
        pieces: List<Piece>,
        moves: String
    ): List<Piece> {
        val playerPiecesWithMoves = gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(pieces, playerColor, moves)
        val enemyPieces = gameEngine.getEnemyPieces(pieces, playerColor)

        return playerPiecesWithMoves.plus(enemyPieces)
    }
}