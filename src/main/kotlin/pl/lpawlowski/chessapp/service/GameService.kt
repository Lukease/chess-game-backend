package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class GameService(
    private val gamesRepository: GamesRepository,
) {
    @Transactional
    fun getUserGame(user: User): List<GameDto?> {
        return gamesRepository.findByUser(user).stream().map(GameDto::fromDomain).collect(Collectors.toList())
    }

    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Long {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds

            if (gameCreateRequest.isWhitePlayer) {
                whitePlayer = user
            } else {
                blackPlayer = user
            }
        }

        val save: Game = gamesRepository.save(game)

        return save.id!!
    }

    @Transactional
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): GameDto {
        val game =
            gamesRepository.findById(gameMakeMoveRequest.gameId).orElseThrow { RuntimeException("Game not found!") }

        game.moves = "${game.moves},${gameMakeMoveRequest.move}"

        if (user == game.whitePlayer) {
            game.lastMoveWhite = LocalDateTime.now()
        } else {
            game.lastMoveBlack = LocalDateTime.now()
        }

        return GameDto.fromDomain(game)
    }

    @Transactional
    fun joinGame(user: User, joinGameRequest: JoinGameRequest): GameDto {
        val game = gamesRepository.findById(joinGameRequest.gameId).orElseThrow { RuntimeException("Game not found!") }

        if (game.whitePlayer == null) {
            game.whitePlayer = user
        } else {
            game.blackPlayer = user
        }

        game.lastMoveWhite = LocalDateTime.now()
        game.gameStatus = "game"

        return GameDto.fromDomain(game)
    }
}