package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.repositories.GamesRepository

class GameServiceTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var userService: UserService

    @AfterEach
    fun cleanUpDatabase() {
        gamesRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun testCreateGame() {
        insertUser()

        val allUsers = userRepository.findAll()
        val user = allUsers[0]

        gameService.createGame(user, GameCreateRequest(true, 800))

        val games = gamesRepository.findAll()

        assertThat(games.size).isEqualTo(1)
        assertThat(games[0].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[0].whitePlayer).isNotNull
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
        val moves = "A4"

        gameService.joinGame(joiningUser, JoinGameRequest(correctGameId))
        gameService.makeMove(whitePlayer, GameMakeMoveRequest(moves))

        val gameInProgress = gamesRepository.findAll()[0]

        assertThat(gameInProgress.moves).isNotEqualTo("")
        assertThat(gameInProgress.moves).isEqualTo(moves)
    }

    @Test
    fun testJoinGame() {
        insertUser()

        val allUsers = userRepository.findAll()
        val user = allUsers[0]

        val correctGameId = gameService.createGame(user, GameCreateRequest(true, 800))

        insertUser(testUserLogin)

        val joiningUser = userService.findUserByLogin(testUserLogin)
        val wrongGameId: Long = -1

        assertThrows<RuntimeException> { gameService.joinGame(joiningUser, JoinGameRequest(wrongGameId)) }

        gameService.joinGame(joiningUser, JoinGameRequest(correctGameId))

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

        val gameId = gameService.createGame(firstUser, GameCreateRequest(true, 800))
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
        val gameId = gameService.createGame(whitePlayer, GameCreateRequest(true, 800))
        val joinGameRequest = JoinGameRequest(gameId)

        gameService.joinGame(blackPlayer, joinGameRequest)

        val gameResponse = gameService.getUserActiveGameAndReturnMoves(blackPlayer)
        val secondUserColor = if (gameResponse.gameDto.whitePlayer?.login == blackPlayer.login) "white" else "black"

        assertThat(gameResponse.gameDto.whitePlayer?.login).isEqualTo(whitePlayer.login)
        assertThat(gameResponse.gameDto.blackPlayer?.login).isEqualTo(blackPlayer.login)
        assertThat(gameResponse.whoseTurn).isEqualTo(secondUserColor)
    }
}