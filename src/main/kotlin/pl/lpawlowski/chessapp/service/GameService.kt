package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.ForbiddenUser
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.web.chess_possible_move.Move
import pl.lpawlowski.chessapp.web.chess_possible_move.MoveHistory
import pl.lpawlowski.chessapp.web.pieces.Piece
import java.time.Duration
import java.time.LocalDateTime

@Service
class GameService(
    private val gamesRepository: GamesRepository,
    private val gameEngine: GameEngine,
    private val drawOffersRepository: DrawOffersRepository,
    private val fenConverter: FenConverter
) {
    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Game {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds
            currentFen = gameCreateRequest.startingFen ?: gameEngine.getDefaultFen()
            timeLeftBlack = gameCreateRequest.timePerPlayerInSeconds
            timeLeftWhite = gameCreateRequest.timePerPlayerInSeconds
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
        val pieces = fenConverter.convertFenToPiecesList(game.currentFen)
        val moves = game.history.split(",")
        val whoseTurn = when {
            moves.size % 2 == 0 || moves.first() == "" -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val playerColor = when (user.login) {
            game.whitePlayer!!.login -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val enemyColor = if (playerColor == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE

        when (playerColor) {
            whoseTurn -> {
                val lastMove =
                    if (moves.first() != "") getGameLastMove(
                        game.previousFen,
                        game.historyExtended,
                        enemyColor,
                        moves
                    ) else null
                val piecesWithCorrectMoves = getPieceWithCorrectMovesOfPlayer(playerColor, pieces, lastMove)
                val move: Move = gameEngine.convertStringToMove(
                    gameMakeMoveRequest.pieceFromId,
                    gameMakeMoveRequest.fieldToId,
                    gameMakeMoveRequest.promotedPieceName,
                    piecesWithCorrectMoves
                )
                val currentFen = fenConverter.convertPieceListToFen(move.pieces)

                game.previousFen = game.currentFen
                game.currentFen = currentFen
                game.historyExtended += if (game.historyExtended == "") move.nameOfMoveFromTo else ",${move.nameOfMoveFromTo}"

                val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(pieces, playerColor)
                val playerKing = findKing(move.pieces, playerColor)
                val enemyKing = findKing(enemyPieces, enemyColor)
                val piecesWithCorrectMovesAfterMove = getPieceWithCorrectMovesOfPlayer(playerColor, pieces, lastMove)
                val checkedKingsId = getCheckedKingsId(enemyPieces, piecesWithCorrectMovesAfterMove)
                val enemyPiecesWithFilteredMoves = gameEngine.filterMovesDontCauseCheckAndCoveringKingPieces(
                    enemyPieces,
                    enemyColor,
                    piecesWithCorrectMoves
                )

                addIsCheckToMoveAndChangeMoveTime(game, checkedKingsId, enemyKing, move, user)
                checkIfPlayerDontHaveMoveChangeGameStatus(
                    enemyPiecesWithFilteredMoves,
                    enemyColor,
                    checkedKingsId,
                    enemyKing,
                    game
                )

                val pieceDto = move.pieces.map { PieceDto.fromDomain(it) }

                return MakeMoveResponse(
                    pieceDto,
                    GameDto.fromDomain(game),
                    whoseTurn.name,
                    playerColor.name,
                    checkedKingsId,
                    move.fieldFrom,
                    move.fieldTo
                )
            }

            else -> throw ForbiddenUser("It's not your turn!")
        }
    }

    @Transactional
    fun getUserActiveGameAndReturnMoves(user: User): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = fenConverter.convertFenToPiecesList(game.currentFen)
        val moves = game.history.split(",")
        val whoseTurn = when {
            moves.size % 2 == 0 || moves.first() == "" -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val playerColor = when (user.login) {
            game.whitePlayer!!.login -> PlayerColor.WHITE
            else -> PlayerColor.BLACK
        }
        val enemyColor = when (whoseTurn) {
            PlayerColor.WHITE -> PlayerColor.BLACK
            else -> PlayerColor.WHITE
        }
        val lastMove =
            if (moves.first() != "") getGameLastMove(
                game.previousFen,
                game.historyExtended,
                enemyColor,
                moves
            ) else null

        val updatedGame = when (playerColor) {
            whoseTurn -> updateMoveTime(user, game)
            else -> game
        }

        val piecesWithCorrectMoves =
            if (playerColor == whoseTurn) getPieceWithCorrectMovesOfPlayer(
                playerColor,
                pieces,
                lastMove
            ) else pieces
        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(pieces, playerColor)
        val checkedKingsId = getCheckedKingsId(enemyPieces, piecesWithCorrectMoves)
        val filteredPieces = piecesWithCorrectMoves.map { piece: Piece ->
            when {
                playerColor == whoseTurn && piece.color == playerColor -> PieceDto.fromDomain(piece)
                else -> {
                    piece.possibleMoves = emptyList()
                    PieceDto.fromDomain(piece)
                }
            }
        }

        return MakeMoveResponse(
            filteredPieces,
            GameDto.fromDomain(updatedGame),
            whoseTurn.name.lowercase(),
            playerColor.name.lowercase(),
            checkedKingsId,
            lastMove?.fieldFrom,
            lastMove?.fieldTo
        )
    }

    @Transactional
    fun resign(user: User): Game {
        val game = gamesRepository.findActiveGamesByUser(user, GameStatus.FINISHED.name)
            .orElseThrow { NotFound("User does not have an active game!") }

        game.gameStatus = GameStatus.FINISHED.name
        game.result = when (user.login) {
            game.whitePlayer!!.login -> GameResult.BLACK.name
            else -> GameResult.WHITE.name
        }

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
            .orElseThrow { NotFound("Active game not found!") }
    }

    fun getPieceWithCorrectMovesOfPlayer(
        playerColor: PlayerColor,
        pieces: List<Piece>,
        lastMove: MoveHistory?
    ): List<Piece> {
        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(pieces, playerColor)
        val playerPiecesWithMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(pieces, playerColor, enemyPieces, lastMove)

        return playerPiecesWithMoves.plus(enemyPieces)
    }

    private fun getGameLastMove(
        previousFen: String,
        moveFromTo: String,
        enemyColor: PlayerColor,
        moves: List<String>
    ): MoveHistory {
        val pieces = fenConverter.convertFenToPiecesList(previousFen)
        val enemyPieceWithMoves = gameEngine.calculateAndReturnCaptureMoveOfEnemy(pieces, enemyColor)
        val lastMove = moveFromTo.split(",").last()
        val checkCastleMove = moves.last()

        return gameEngine.checkLastMove(lastMove, enemyPieceWithMoves, enemyColor, checkCastleMove)
    }

    private fun checkPlayerHavePossibleMoves(
        piecesWithCorrectMoves: List<Piece>,
        playerColor: PlayerColor
    ): Boolean {
        return piecesWithCorrectMoves.any { it.color == playerColor && it.possibleMoves.isNotEmpty() }
    }

    fun getCheckedKingsId(
        enemyPieces: List<Piece>,
        playerPieces: List<Piece>
    ): List<String> {
        val whitePlayerKing = findKing(playerPieces, PlayerColor.WHITE)
        val blackPlayerKing = findKing(playerPieces, PlayerColor.BLACK)
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

    private fun checkIfPlayerDontHaveMoveChangeGameStatus(
        piecesWithCorrectMoves: List<Piece>,
        playerColor: PlayerColor,
        checkedKingsId: List<String>,
        playerKing: Piece,
        game: Game
    ): Game {
        val playerHaveMove = checkPlayerHavePossibleMoves(piecesWithCorrectMoves, playerColor)
        when {
            !playerHaveMove && checkedKingsId.contains(playerKing.id) -> {
                game.gameStatus = GameStatus.FINISHED.name
                game.result =
                    if (playerColor == PlayerColor.WHITE) GameResult.BLACK.name else GameResult.WHITE.name
            }

            !playerHaveMove -> {
                game.gameStatus = GameStatus.FINISHED.name
                game.result = GameResult.DRAW.name
            }
        }
        return game
    }

    private fun addIsCheckToMoveAndChangeMoveTime(
        game: Game,
        checkedKingsId: List<String>,
        enemyKing: Piece,
        move: Move,
        user: User
    ): Game {
        val isCheck = if (checkedKingsId.contains(enemyKing.id)) "+" else ""
        val moveName = move.nameOfMove.plus(isCheck)

        game.history = when (game.history.isBlank()) {
            true -> moveName
            false -> "${game.history},$moveName"
        }

        return updateMoveTime(user, game)
    }

    private fun updateMoveTime(user: User, game: Game): Game {
        val moveTime = LocalDateTime.now()

        when (user.login) {
            game.whitePlayer!!.login -> {
                val timeElapsed = Duration.between(game.lastMoveWhite, moveTime).seconds

                game.lastMoveWhite = moveTime
                game.timeLeftWhite -= timeElapsed.toInt()

                when {
                    game.timeLeftWhite <= 0 -> {
                        game.result = GameResult.BLACK.name
                        game.gameStatus = GameStatus.FINISHED.name
                    }
                }

                game.lastMoveBlack = game.lastMoveBlack ?: moveTime

            }

            else -> {
                val timeElapsed = Duration.between(game.lastMoveBlack, moveTime).seconds

                game.lastMoveBlack = moveTime
                game.timeLeftBlack -= timeElapsed.toInt()

                when {
                    game.timeLeftBlack <= 0 -> {
                        game.result = GameResult.WHITE.name
                        game.gameStatus = GameStatus.FINISHED.name
                    }
                }
            }
        }

        return game
    }
}