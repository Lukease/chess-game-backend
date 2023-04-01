package pl.lpawlowski.chessapp.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.model.game.*
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
    ): MakeMoveResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.makeMove(user, gameMakeMoveRequest)
    }

    @PostMapping("/join-game")
    fun joinGame(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody joinGameRequest: JoinGameRequest
    ): JoinGameResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.joinGame(user, joinGameRequest)
    }

    @GetMapping("/get-all")
    fun getAllCreatedGames(): List<GameDto> {
        return gameService.getAllCreatedGames()
    }

    @GetMapping("/get-active")
    fun getUserActiveGame(@RequestHeader("Authorization") authorization: String): GameDto? {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getUserActiveGame(user)
    }

    @GetMapping("/get-in-progress")
    fun getUserActiveGameAndReturnMoves(@RequestHeader("Authorization") authorization: String): MakeMoveResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getUserActiveGameAndReturnMoves(user)
    }

    @PutMapping("/resign")
    fun resign(
        @RequestHeader("Authorization") authorization: String
    ) {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        gameService.resign(user)
    }
}