package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.time.LocalDateTime

@SpringBootTest
class GameServiceTests {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @AfterEach
    fun cleanUpDatabase() {
        gamesRepository.deleteAll()
    }

    @Test
    fun testCreateGame() {
        val gameDto = GameDto(
            moves = "B3",
            lastMoveBlack = LocalDateTime.now(),
            lastMoveWhite = LocalDateTime.now()
        )

        gameService.createGame(gameDto)

        val allGames = gamesRepository.findAll()

        assertThat(allGames.size).isEqualTo(1)
    }

    @Test
    fun testUpdateGame() {
        val gameDto = GameDto(
            moves = "A3",
            lastMoveBlack = LocalDateTime.now(),
            lastMoveWhite = LocalDateTime.now()
        )

        gameService.createGame(gameDto)

        val game = gamesRepository.findAll()[0]
        val newGameData: Game = Game().apply {
            moves = "A3,B6,D4,C3"
            lastMoveWhite = LocalDateTime.now()
            lastMoveBlack = LocalDateTime.now()
        }

        gameService.makeMove(game.id!!,newGameData)

        val updateGame = gamesRepository.findAll()[0]

        assertThat(updateGame.moves).isEqualTo(newGameData.moves)
        assertThat(updateGame.lastMoveWhite.withNano(0)).isEqualTo(newGameData.lastMoveWhite.withNano(0))
        assertThat(updateGame.lastMoveBlack.withNano(0)).isEqualTo(newGameData.lastMoveBlack.withNano(0))
    }
}