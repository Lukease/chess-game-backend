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

@SpringBootTest
class DrawOffersTests {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var userRepository: UsersRepository

    @Autowired
    lateinit var drawOffersService: DrawOffersService

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    @AfterEach
    fun cleanUpDatabase() {
        drawOffersRepository.deleteAll()
    }

    @Test
    fun testCreateOffer() {
        val firstUserDto = UserDto(
            login = "Adam",
            password = "Adam12345!",
            email = "Adam@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "kuba",
            password = "kuba12345!",
            email = "kuba@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[0]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))

        val game = gamesRepository.findAll()[0]

        gameService.joinGame(secondUser, JoinGameRequest(game.id!!))
        drawOffersService.createOffer(firstUser)

        val drawOffers = drawOffersRepository.findAll()

        assertThat(drawOffers.size).isEqualTo(1)
    }

    @Test
    fun testResponseOffer() {
        val firstUserDto = UserDto(
            login = "Andrzej",
            password = "Andrzej12345!",
            email = "Andrzej@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "Damian",
            password = "Damian12345!",
            email = "Damian@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[0]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))

        val game = gamesRepository.findAll()[0]

        gameService.joinGame(secondUser, JoinGameRequest(game.id!!))
        drawOffersService.createOffer(firstUser)
        drawOffersService.responseOffer(secondUser, GameDrawOfferRequest(game.id!!, false))

        val drawOffers = drawOffersRepository.findAll()

        assertThat(drawOffers[0].game.gameStatus).isEqualTo(GameStatus.IN_PROGRESS.name)
        assertThat(drawOffers[0].status).isEqualTo(DrawOffersStatus.REJECTED.name)

        drawOffersService.createOffer(firstUser)
        drawOffersService.responseOffer(secondUser, GameDrawOfferRequest(game.id!!, true))

        val twoDrawOffers = drawOffersRepository.findAll()

        assertThat(twoDrawOffers[1].game.gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(twoDrawOffers[1].game.result).isEqualTo(GameResult.DRAW.name)
        assertThat(twoDrawOffers[1].status).isEqualTo(DrawOffersStatus.ACCEPTED.name)
    }

    @Test
    fun getDrawOffer() {
        val firstUserDto = UserDto(
            login = "Kamil",
            password = "Kamil12345!",
            email = "Kamil@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "Mateusz",
            password = "Mateusz12345!",
            email = "Mateusz@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[0]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))

        val game = gamesRepository.findAll()[0]

        gameService.joinGame(secondUser, JoinGameRequest(game.id!!))
        drawOffersService.createOffer(firstUser)

        val allOffer = drawOffersRepository.findAll()
        val offerDto = DrawOffersDto.fromDomain(allOffer[0])
        val offer = drawOffersService.getDrawOffer(secondUser)

        assertThat(offer.status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(offer.id).isEqualTo(offerDto.id)
    }
}