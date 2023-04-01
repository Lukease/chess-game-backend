package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.repositories.UsersRepository

class GameServiceTests: BasicIntegrationTest() {
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
        val userDto = UserDto(
            login = "Maciek",
            password = "Maciek12345!",
            email = "Maciek@onet.pl"
        )

        userService.saveUser(userDto)

        val allUsers = userRepository.findAll()
        val user = allUsers[0]

        gameService.createGame(user, GameCreateRequest(true, 800))

        val games = gamesRepository.findAll()

        assertThat(games.size).isEqualTo(1)
        assertThat(games[0].gameStatus).isEqualTo(GameStatus.CREATED.name)
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
        val userDto = UserDto(
            login = "jan",
            password = "jan12345",
            email = "jan@onet.pl"
        )

        userService.saveUser(userDto)

        val allUsers = userRepository.findAll()
        val user = allUsers[0]

        gameService.createGame(user, GameCreateRequest(true, 800))

        val allGames = gamesRepository.findAll()
        val createdGame = allGames[0]
        val joiningUserDto = UserDto(
            login = "Daniel",
            password = "Daniel12345",
            email = "daniel@onet.pl"
        )

        userService.saveUser(joiningUserDto)

        val allUsersAfterJoining = userRepository.findAll()
        val joiningUser = allUsersAfterJoining[1]
        val correctGameId: Long = createdGame.id!!
        val wrongGameId: Long = -1

        assertThrows<RuntimeException> { gameService.joinGame(joiningUser, JoinGameRequest(wrongGameId)) }
        assertDoesNotThrow { gameService.joinGame(joiningUser, JoinGameRequest(correctGameId)) }

        val gameWithJoinedPlayer = gamesRepository.findAll()[0]

        assertThat(gameWithJoinedPlayer.whitePlayer?.login).isEqualTo(user.login)
        assertThat(gameWithJoinedPlayer.blackPlayer?.login).isEqualTo(joiningUser.login)
    }

    @Test
    fun testGetAllCreatedGame() {
        val firstUserDto = UserDto(
            login = "arek",
            password = "arek12345",
            email = "arek@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "Dawid",
            password = "Dawid12345",
            email = "dawid@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[1]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))
        gameService.createGame(secondUser, GameCreateRequest(false, 200))
        assertDoesNotThrow { gameService.getAllCreatedGames() }

        val games = gameService.getAllCreatedGames()

        assertThat(games.size).isEqualTo(2)
        assertThat(games[0].gameStatus).isEqualTo(GameStatus.CREATED.name)
        assertThat(games[1].gameStatus).isEqualTo(GameStatus.CREATED.name)
    }

    @Test
    fun testResignGame() {
        val firstUserDto = UserDto(
            login = "arek",
            password = "arek12345",
            email = "arek@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "Dawid",
            password = "Dawid12345",
            email = "dawid@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[1]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))

        val gameId = gamesRepository.findAll()[0].id
        val joinGameRequest = JoinGameRequest(gameId!!)

        gameService.joinGame(secondUser, joinGameRequest)

        val gameInProgress = gamesRepository.findAll()[0]

        assertThat(gameInProgress.gameStatus).isEqualTo(GameStatus.IN_PROGRESS.name)
        gameService.resign(secondUser)

        val gameResign = gamesRepository.findAll()[0]

        assertThat(gameResign.gameStatus).isEqualTo(GameStatus.FINISHED.name)
    }

    @Test
    fun testGetActiveGameAndReturnMoves() {
        val firstUserDto = UserDto(
            login = "arek",
            password = "arek12345",
            email = "arek@onet.pl"
        )

        val secondUserDto = UserDto(
            login = "Dawid",
            password = "Dawid12345",
            email = "dawid@onet.pl"
        )

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)

        val allUsers = userRepository.findAll()
        val firstUser = allUsers[0]
        val secondUser = allUsers[1]

        gameService.createGame(firstUser, GameCreateRequest(true, 800))

        val gameId = gamesRepository.findAll()[0].id
        val joinGameRequest = JoinGameRequest(gameId!!)

        gameService.joinGame(secondUser, joinGameRequest)

        val gameResponse = gameService.getUserActiveGameAndReturnMoves(secondUser)
        val secondUserColor = if (gameResponse.gameDto.whitePlayer?.login == secondUser.login) "white" else "black"
        val piecesColor = gameResponse.pieces[0].color

        assertThat(gameResponse.gameDto.whitePlayer?.login).isEqualTo(firstUser.login)
        assertThat(gameResponse.gameDto.blackPlayer?.login).isEqualTo(secondUser.login)
        assertThat(gameResponse.pieces.size).isNotEqualTo(0)
        assertThat(piecesColor).isEqualTo(secondUserColor)
    }
}