package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.ForbiddenUser
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class FinishGameTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    @AfterEach
    fun cleanUpDatabase() {
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun testDraw() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100, "3k4/8/7R/8/8/8/8/K1R1R3")
        val gameMakeWhiteMoveRequest = GameMakeMoveRequest("H6", "H7", null)

        gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        val gameAfterMove = gamesRepository.findById(game.id!!)

        assertThat(gameAfterMove.get().gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(gameAfterMove.get().result).isEqualTo(GameResult.DRAW.name)
    }

    @Test
    fun testCheckMat() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val game = createGame(userBlack, userWhite, 100, "3k4/7R/8/8/8/8/7Q/K1R1R3")
        val gameMakeWhiteMoveRequest = GameMakeMoveRequest("H2", "D2", null)

        gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        val gameAfterMove = gamesRepository.findById(game.id!!)

        assertThat(gameAfterMove.get().gameStatus).isEqualTo(GameStatus.FINISHED.name)
        assertThat(gameAfterMove.get().result).isEqualTo(GameResult.WHITE.name)
    }

}