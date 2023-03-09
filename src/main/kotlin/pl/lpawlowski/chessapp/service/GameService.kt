package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.time.LocalDateTime

@Service
class GameService(
    private val gamesRepository: GamesRepository,
) {
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
    fun makeMove(user: User, gameMakeMoveRequest: GameMakeMoveRequest): MakeMoveResponse {
        val game = getUserGame(user)
        val pieces = listOf<Piece>()
        val isCheck = true
        val whoseTurn = "black"
        val lastPlayerMove = LocalDateTime.now()
        val historyOfMoves = ""


        game.moves = when (game.moves.isBlank()) {
            true -> gameMakeMoveRequest.move
            false -> "${game.moves},${gameMakeMoveRequest.move}"
        }
        if (user == game.whitePlayer) {
            game.lastMoveWhite = LocalDateTime.now()
        } else {
            game.lastMoveBlack = LocalDateTime.now()
        }

        return MakeMoveResponse(pieces, isCheck, whoseTurn, lastPlayerMove, historyOfMoves)
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
        game.gameStatus = GameStatus.IN_PROGRESS.name

        return GameDto.fromDomain(game)
    }

    private fun getUserGame(user: User): Game {
        return gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { RuntimeException("Game not found!") }
    }
}