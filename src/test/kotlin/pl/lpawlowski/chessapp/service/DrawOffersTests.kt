package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.offers.DrawOffersDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.repositories.UsersRepository

class DrawOffersTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var drawOffersService: DrawOffersService

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    @AfterEach
    fun cleanUpDatabase() {
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
    }

    @Test
    fun testCreateOffer() {
        val secondUserLogin = "Adam"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800))

        gameService.joinGame(secondUser, JoinGameRequest(gameId))
        drawOffersService.createOffer(firstUser)

        val game = gamesRepository.findById(gameId).get()

        assertThat(game.drawOffers.size).isEqualTo(1)
    }

    @Test
    fun testResponseOffer() {
        val secondUserLogin = "Adam"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800))

        gameService.joinGame(secondUser, JoinGameRequest(gameId))
        drawOffersService.createOffer(firstUser)

        val firstDrawId = drawOffersService.responseOffer(secondUser, GameDrawOfferRequest(gameId, false))
        val firstDrawOffer = drawOffersRepository.findById(firstDrawId).get()

        assertThat(firstDrawOffer.game.gameStatus).isEqualTo(GameStatus.IN_PROGRESS.name)
        assertThat(firstDrawOffer.status).isEqualTo(DrawOffersStatus.REJECTED.name)
        drawOffersService.createOffer(firstUser)

        val secondDrawId = drawOffersService.responseOffer(secondUser, GameDrawOfferRequest(gameId, true))
        val secondDrawOffer = drawOffersRepository.findById(secondDrawId).get()

        assertThat(secondDrawOffer.game.gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(secondDrawOffer.game.result).isEqualTo(GameResult.DRAW.name)
        assertThat(secondDrawOffer.status).isEqualTo(DrawOffersStatus.ACCEPTED.name)
    }

    @Test
    fun getDrawOffer() {
        val secondUserLogin = "Adam"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800))

        gameService.joinGame(secondUser, JoinGameRequest(gameId))

        val drawOfferId = drawOffersService.createOffer(firstUser)
        val offer = drawOffersService.getDrawOffer(secondUser)

        assertThat(offer.status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(offer.id).isEqualTo(drawOfferId)
    }
}