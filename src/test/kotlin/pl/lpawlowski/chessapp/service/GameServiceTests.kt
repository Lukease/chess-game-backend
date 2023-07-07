package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GameServiceTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    @AfterEach
    fun cleanUpDatabase() {
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun testCreateGame() {
        val whiteUser = insertUser(testUserLogin1)
        val timePerPlayerInSeconds = 800
        val game = gameService.createGame(whiteUser, GameCreateRequest(true, timePerPlayerInSeconds, null))

        assertThat(game.gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(game.whitePlayer!!.login).isEqualTo(whiteUser.login)
        assertThat(game.timePerPlayerInSeconds).isEqualTo(timePerPlayerInSeconds)
        assertThat(game.history).isEqualTo("")
    }

    @Test
    fun testJoinGame() {
        val whiteUser = insertUser(testUserLogin1)
        val blackUser = insertUser(testUserLogin2)
        val timePerPlayerInSeconds = 800
        val game = gameService.createGame(whiteUser, GameCreateRequest(true, timePerPlayerInSeconds, null))

        gameService.joinGame(blackUser, JoinGameRequest(game.id!!))

        val gameWithJoinedPlayer = gamesRepository.findById(game.id!!)

        assertThat(gameWithJoinedPlayer.get().whitePlayer?.login).isEqualTo(whiteUser.login)
        assertThat(gameWithJoinedPlayer.get().blackPlayer?.login).isEqualTo(blackUser.login)
        assertThat(gameWithJoinedPlayer.get().id).isEqualTo(game.id)
    }

    @Test
    fun testJoinWrongGame() {
        val whiteUser = insertUser(testUserLogin1)
        val blackUser = insertUser(testUserLogin2)
        val timePerPlayerInSeconds = 800

        gameService.createGame(whiteUser, GameCreateRequest(true, timePerPlayerInSeconds, null))

        val wrongGameId: Long = -1

        assertThrows<RuntimeException> { gameService.joinGame(blackUser, JoinGameRequest(wrongGameId)) }
    }

    @Test
    fun testGetAllCreatedGame() {
        val secondUserLogin = "Dawid"

        insertUser(testUserLogin1)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin1)
        val secondUser = userService.findUserByLogin(secondUserLogin)

        gameService.createGame(firstUser, GameCreateRequest(true, 800, null))
        gameService.createGame(secondUser, GameCreateRequest(false, 200, null))

        val games = gameService.getAllCreatedGames()

        assertThat(games.size).isEqualTo(2)
        assertThat(games[0].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[0].whitePlayer!!.login).isEqualTo(testUserLogin1)
        assertThat(games[1].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[1].blackPlayer!!.login).isEqualTo(secondUserLogin)
    }

    @Test
    fun testResignGame() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100)

        gameService.resign(userBlack)

        val gameResign = gamesRepository.findById(game.id!!).orElseThrow { NotFound("Game not found!") }

        assertThat(gameResign.gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(gameResign.result).isEqualTo(GameResult.WHITE.name)
        assertThat(gameResign.blackPlayer!!.login).isEqualTo(userBlack.login)
        assertThat(gameResign.whitePlayer!!.login).isEqualTo(userWhite.login)
        assertThat(gameResign.id).isEqualTo(game.id)
    }

    @Test
    fun testGetActiveGameAndReturnMovesOfPlayerWhichIsTurn() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100)
        val gameResponse = gameService.getUserActiveGameAndReturnMoves(userWhite)

        assertThat(gameResponse.whoseTurn).isEqualTo(PlayerColor.WHITE.name.lowercase())
        assertThat(gameResponse.fieldFrom).isEqualTo(null)
        assertThat(gameResponse.fieldTo).isEqualTo(null)
        assertThat(gameResponse.playerColor).isEqualTo(PlayerColor.WHITE.name.lowercase())
        assertThat(gameResponse.pieces.size).isEqualTo(32)
        assertThat(gameResponse.pieces.filter { it.color == PlayerColor.WHITE.name.lowercase() }
            .any { it.possibleMoves.isNotEmpty() }).isTrue
        assertThat(gameResponse.pieces.filter { it.color != PlayerColor.WHITE.name.lowercase() }
            .any { it.possibleMoves.isEmpty() }).isTrue
        assertThat(gameResponse.gameInfo.id).isEqualTo(game.id)
        assertThat(gameResponse.kingIsChecked.size).isEqualTo(0)
    }

    @Test
    fun testGetActiveGameAndReturnMovesOfPlayerWhichIsNotTurn() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100)
        val gameResponse = gameService.getUserActiveGameAndReturnMoves(userBlack)

        assertThat(gameResponse.whoseTurn).isNotEqualTo(PlayerColor.BLACK.name.lowercase())
        assertThat(gameResponse.fieldFrom).isEqualTo(null)
        assertThat(gameResponse.fieldTo).isEqualTo(null)
        assertThat(gameResponse.playerColor).isEqualTo(PlayerColor.BLACK.name.lowercase())
        assertThat(gameResponse.pieces.size).isEqualTo(32)
        assertThat(gameResponse.pieces.all { it.possibleMoves.isEmpty() }).isTrue
        assertThat(gameResponse.gameInfo.id).isEqualTo(game.id)
        assertThat(gameResponse.kingIsChecked.size).isEqualTo(0)
    }

    @Test
    fun testIsChangedTimeOfLastMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)

        createGame(userBlack, userWhite, 100)
        Thread.sleep(1000)

        val gameResponse = gameService.getUserActiveGameAndReturnMoves(userBlack)

        assertThat(gameResponse.gameInfo.lastMoveBlack).isNull()
        assertThat(gameResponse.gameInfo.timeLeftBlack).isEqualTo(100)

        val gameMakeWhiteMoveRequest = GameMakeMoveRequest("A2", "A3", null)
        val gameAfterWhiteMove = gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        assertThat(gameAfterWhiteMove.gameInfo.lastMoveWhite).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
        assertThat(gameAfterWhiteMove.gameInfo.lastMoveBlack).isNotNull
        assertThat(gameAfterWhiteMove.gameInfo.timeLeftWhite).isNotEqualTo(100)
        assertThat(gameAfterWhiteMove.gameInfo.moves).isNotEqualTo("")
        Thread.sleep(1000)

        val gameMakeBlackMoveRequest = GameMakeMoveRequest("A7", "A6", null)
        val gameAfterBlackMove = gameService.makeMove(userBlack, gameMakeBlackMoveRequest)

        assertThat(gameAfterBlackMove.gameInfo.lastMoveBlack).isNotEqualTo(gameAfterWhiteMove.gameInfo.lastMoveBlack)
        assertThat(gameAfterBlackMove.gameInfo.timeLeftBlack).isNotEqualTo(100)
    }

    @Test
    fun testCreateOffer() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100)

        gameService.createOffer(userWhite)

        val gameAfterOffer = gamesRepository.findById(game.id!!).get()

        assertThat(gameAfterOffer.drawOffers.size).isEqualTo(1)
        assertThat(gameAfterOffer.drawOffers[0].status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(gameAfterOffer.drawOffers[0].game.id).isEqualTo(game.id)
        assertThat(gameAfterOffer.drawOffers[0].playerOffered.login).isEqualTo(userWhite.login)
        assertThat(gameAfterOffer.drawOffers[0].playerResponding.login).isEqualTo(userBlack.login)
    }

    @Test
    fun testResponseOffer() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100)

        gameService.createOffer(userWhite)

        val firstDrawId = gameService.responseOffer(userBlack, GameDrawOfferRequest(game.id!!, false))
        val firstDrawOffer = drawOffersRepository.findById(firstDrawId).get()

        assertThat(firstDrawOffer.game.gameStatus).isEqualTo(GameStatus.IN_PROGRESS.name)
        assertThat(firstDrawOffer.status).isEqualTo(DrawOffersStatus.REJECTED.name)
        gameService.createOffer(userWhite)

        val secondDrawId = gameService.responseOffer(userBlack, GameDrawOfferRequest(game.id!!, true))
        val secondDrawOffer = drawOffersRepository.findById(secondDrawId).get()

        assertThat(secondDrawOffer.game.gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(secondDrawOffer.game.result).isEqualTo(GameResult.DRAW.name)
        assertThat(secondDrawOffer.status).isEqualTo(DrawOffersStatus.ACCEPTED.name)
    }

    @Test
    fun getDrawOffer() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)

        createGame(userBlack, userWhite, 100)
        gameService.createOffer(userWhite)

        val offer = gameService.getDrawOffer(userBlack)

        assertThat(offer.status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(offer.playerOffered.login).isEqualTo(userWhite.login)
        assertThat(offer.playerResponding.login).isEqualTo(userBlack.login)
    }
}