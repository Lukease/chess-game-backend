package pl.lpawlowski.chessapp.controller

import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.model.history.AllHistoryGamesResponse
import pl.lpawlowski.chessapp.model.history.HistoryRequest
import pl.lpawlowski.chessapp.model.history.HistoryResponse
import pl.lpawlowski.chessapp.model.user.*
import pl.lpawlowski.chessapp.service.HistoryService
import pl.lpawlowski.chessapp.service.UserService

@CrossOrigin(origins = ["http://localhost:3000/"])
@RestController
@RequestMapping(value = ["/history"])
class HistoryController(
    private val historyService: HistoryService,
    private val userService: UserService
) {
    @GetMapping
    fun getAllPlayerGames(
        @RequestHeader("Authorization") authorization: String
    ): AllHistoryGamesResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return historyService.getAllPlayerGames(user)
    }

    @PutMapping("/get-game")
    fun getHistoryFromGame(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody historyRequest: HistoryRequest
    ): HistoryResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return historyService.getHistoryFromGame(historyRequest, user)
    }
}