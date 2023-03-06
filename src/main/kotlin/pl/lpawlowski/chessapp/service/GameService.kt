package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.GamesRepository

@Service
class GameService(
    private val gamesRepository: GamesRepository,
) {

    @Transactional
    fun createGame(user: User, gameCreateRequest: GameCreateRequest): Long {
        val game: Game = Game().apply {
            timePerPlayerInSeconds = gameCreateRequest.timePerPlayerInSeconds
            whitePlayer = if (gameCreateRequest.isWhitePlayer) user else null
            blackPlayer = if (!gameCreateRequest.isWhitePlayer) user else null

        }

        val save: Game = gamesRepository.save(game)

        return save.id!!
    }

    @Transactional
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): GameDto {
        val game = gamesRepository.findByUser(user).orElseThrow { RuntimeException("Game not found!") }

        game.moves = game.moves + gameMakeMoveRequest.moves
        game.lastMoveWhite = if (game.whitePlayer == user) gameMakeMoveRequest.moveTime else game.lastMoveWhite
        game.lastMoveBlack = if (game.blackPlayer == user) gameMakeMoveRequest.moveTime else game.lastMoveBlack

        return GameDto.fromDomain(game)
    }

    @Transactional
    fun joinGame(user: User): GameDto {
        val game = gamesRepository.findByUser(user).orElseThrow { RuntimeException("Game not found!") }

        game.whitePlayer = if (game.whitePlayer == null) user else game.whitePlayer
        game.blackPlayer = if (game.blackPlayer == null) user else game.blackPlayer

        return GameDto.fromDomain(game)
    }
}