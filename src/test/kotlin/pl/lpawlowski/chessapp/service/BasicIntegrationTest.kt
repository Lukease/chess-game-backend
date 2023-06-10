package pl.lpawlowski.chessapp.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.repositories.UsersRepository
import java.time.LocalDateTime

@SpringBootTest
class BasicIntegrationTest {
    @Autowired
    lateinit var userRepository: UsersRepository

    @Autowired
    lateinit var gameRepository: GamesRepository

    val testUserLogin1 = "Kuba"
    val testUserLogin2 = "Sebastian"
    val testsUserEmail = "Kuba123@gmail.com"


    fun createGame(
        blackPlayer: User,
        whitePlayer: User,
        timeForPlayer: Int,
        startingFen: String? = null
    ): Game {
        val game = Game().apply {
            this.blackPlayer = blackPlayer
            this.whitePlayer = whitePlayer
            this.lastMoveWhite = LocalDateTime.now()
            this.currentFen = startingFen ?: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
            this.gameStatus = GameStatus.IN_PROGRESS.name
            this.timePerPlayerInSeconds = timeForPlayer
            this.timeLeftBlack = timeForPlayer
            this.timeLeftWhite = timeForPlayer
        }

        gameRepository.save(game)

        return game
    }

    fun insertUser(login: String? = null): User {
        return userRepository.save(User().also {
            it.login = login ?: RandomStringUtils.randomAlphabetic(7)
            it.email = "${RandomStringUtils.randomAlphabetic(7)}@gmail.com"
            it.password = RandomStringUtils.randomAlphanumeric(7)
            it.activeToken = "abc"
            it.validUtil = LocalDateTime.now().plusMinutes(10)
        })
    }
}