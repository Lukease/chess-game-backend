package pl.lpawlowski.chessapp.controller

import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.GameCreateRequest
import pl.lpawlowski.chessapp.model.game.GameDto
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.model.game.JoinGameRequest
import pl.lpawlowski.chessapp.model.user.*
import pl.lpawlowski.chessapp.service.GameService
import pl.lpawlowski.chessapp.service.UserService

@CrossOrigin(origins = ["http://localhost:3000/"])
@RestController
@RequestMapping(value = ["/games"])
class GameController(
    private val gameService: GameService,
    private val userService: UserService
) {
    @GetMapping("/get-all")
    fun getUserGame(
        @RequestHeader("Authorization") authorization: String
    ): List<GameDto?> {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getUserGame(user)
    }

    @PostMapping
    fun newGame(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody gameCreateRequest: GameCreateRequest
    ): Long {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.createGame(user, gameCreateRequest)
    }

    @PostMapping("/make-move")
    fun makeMove(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody gameMakeMoveRequest: GameMakeMoveRequest
    ): GameDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.makeMove(user, gameMakeMoveRequest)
    }

    @PostMapping("/join-game")
    fun joinGame(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody joinGameRequest: JoinGameRequest
    ): GameDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.joinGame(user, joinGameRequest)
    }
}