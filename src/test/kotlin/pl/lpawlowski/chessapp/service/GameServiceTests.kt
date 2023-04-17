package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository

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
        userRepository.deleteAll()
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
    }

    @Test
    fun testCreateGame() {
        insertUser()

        val allUsers = userRepository.findAll()
        val user = allUsers[0]
        val timePerPlayerInSeconds = 800

        val game = gameService.createGame(user, GameCreateRequest(true, timePerPlayerInSeconds))

        assertThat(game.gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(game.whitePlayer).isNotNull
        assertThat(game.timePerPlayerInSeconds).isEqualTo(timePerPlayerInSeconds)
        assertThat(game.moves).isEqualTo("")
    }

    @Test
    fun testMakeMove() {
        val userDto = UserDto(
            login = "Jarek",
            password = "Jarek1235!",
            email = "Jarek@onet.pl"
        )

        userService.saveUser(userDto)

        val allUsers = userRepository.findAll()
        val whitePlayer = allUsers[0]

        gameService.createGame(whitePlayer, GameCreateRequest(true, 800))

        val allGames = gamesRepository.findAll()
        val createdGame = allGames[0]
        val joiningUserDto = UserDto(
            login = "Kuba",
            password = "Kuba12345",
            email = "kuba@onet.pl"
        )

        userService.saveUser(joiningUserDto)

        val allUsersAfterJoining = userRepository.findAll()
        val joiningUser = allUsersAfterJoining[1]
        val correctGameId: Long = createdGame.id!!
        val movedPiece = PiecesNames.PAWN.name
        val moves = GameMakeMoveRequest("A5", movedPiece, "")

        gameService.joinGame(joiningUser, JoinGameRequest(correctGameId))
        gameService.makeMove(whitePlayer, moves)

        val gameInProgress = gamesRepository.findAll()[0]

        assertThat(gameInProgress.moves).isNotEqualTo("")
        assertThat(gameInProgress.moves).isEqualTo(moves)
    }

    @Test
    fun testJoinGame() {
        insertUser()

        val allUsers = userRepository.findAll()
        val user = allUsers[0]

        val correctGame = gameService.createGame(user, GameCreateRequest(true, 800))

        insertUser(testUserLogin)

        val joiningUser = userService.findUserByLogin(testUserLogin)
        val wrongGameId: Long = -1

        assertThrows<RuntimeException> { gameService.joinGame(joiningUser, JoinGameRequest(wrongGameId)) }

        gameService.joinGame(joiningUser, JoinGameRequest(correctGame.id!!))

        val gameWithJoinedPlayer = gamesRepository.findAll()[0]

        assertThat(gameWithJoinedPlayer.whitePlayer?.login).isEqualTo(user.login)
        assertThat(gameWithJoinedPlayer.blackPlayer?.login).isEqualTo(testUserLogin)
    }

    @Test
    fun testGetAllCreatedGame() {
        val secondUserLogin = "Dawid"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)

        gameService.createGame(firstUser, GameCreateRequest(true, 800))
        gameService.createGame(secondUser, GameCreateRequest(false, 200))

        val games = gameService.getAllCreatedGames()

        assertThat(games.size).isEqualTo(2)
        assertThat(games[0].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[0].whitePlayer!!.login).isEqualTo(testUserLogin)
        assertThat(games[1].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[1].blackPlayer!!.login).isEqualTo(secondUserLogin)
    }

    @Test
    fun testResignGame() {
        val secondTestLogin = "Arek"

        insertUser(testUserLogin)
        insertUser(secondTestLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondTestLogin)

        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800)).id!!
        val joinGameRequest = JoinGameRequest(gameId)

        gameService.joinGame(secondUser, joinGameRequest)
        gameService.resign(secondUser)

        val gameResign = gamesRepository.findById(gameId).orElseThrow { NotFound("Game not found!") }

        assertThat(gameResign.gameStatus).isEqualTo(GameStatus.FINISHED.name)
    }

    @Test
    fun testGetActiveGameAndReturnMoves() {
        val secondTestLogin = "Arek"

        insertUser(testUserLogin)
        insertUser(secondTestLogin)

        val whitePlayer = userService.findUserByLogin(testUserLogin)
        val blackPlayer = userService.findUserByLogin(secondTestLogin)
        val gameId = gameService.createGame(whitePlayer, GameCreateRequest(true, 800)).id!!
        val joinGameRequest = JoinGameRequest(gameId)

        gameService.joinGame(blackPlayer, joinGameRequest)

        val gameResponse = gameService.getUserActiveGameAndReturnMoves(blackPlayer)
        val secondUserColor = if (gameResponse.gameInfo.whitePlayer?.login == blackPlayer.login) PlayerColor.WHITE else PlayerColor.BLACK

        assertThat(gameResponse.gameInfo.whitePlayer?.login).isEqualTo(whitePlayer.login)
        assertThat(gameResponse.gameInfo.blackPlayer?.login).isEqualTo(blackPlayer.login)
        assertThat(gameResponse.whoseTurn).isEqualTo(secondUserColor.name)
    }

    @Test
    fun testCreateOffer() {
        val secondUserLogin = "Adam"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800)).id!!

        gameService.joinGame(secondUser, JoinGameRequest(gameId))
        gameService.createOffer(firstUser)

        val game = gamesRepository.findById(gameId).get()

        assertThat(game.drawOffers.size).isEqualTo(1)
        assertThat(game.drawOffers[0].status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(game.drawOffers[0].playerOffered.login).isEqualTo(firstUser.login)
    }

    @Test
    fun testResponseOffer() {
        val secondUserLogin = "Adam"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)

        val firstUser = userService.findUserByLogin(testUserLogin)
        val secondUser = userService.findUserByLogin(secondUserLogin)
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800)).id!!

        gameService.joinGame(secondUser, JoinGameRequest(gameId))
        gameService.createOffer(firstUser)

        val firstDrawId = gameService.responseOffer(secondUser, GameDrawOfferRequest(gameId, false))
        val firstDrawOffer = drawOffersRepository.findById(firstDrawId).get()

        assertThat(firstDrawOffer.game.gameStatus).isEqualTo(GameStatus.IN_PROGRESS.name)
        assertThat(firstDrawOffer.status).isEqualTo(DrawOffersStatus.REJECTED.name)
        gameService.createOffer(firstUser)

        val secondDrawId = gameService.responseOffer(secondUser, GameDrawOfferRequest(gameId, true))
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
        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800)).id!!

        gameService.joinGame(secondUser, JoinGameRequest(gameId))

        val drawOfferId = gameService.createOffer(firstUser)
        val offer = gameService.getDrawOffer(secondUser)

        assertThat(offer.status).isEqualTo(DrawOffersStatus.OFFERED.name)
        assertThat(offer.id).isEqualTo(drawOfferId)
    }
}