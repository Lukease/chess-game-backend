package pl.lpawlowski.chessapp.controller

import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.model.offers.DrawOffersDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
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
    ): GameDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return GameDto.fromDomain(gameService.createGame(user, gameCreateRequest))
    }

    @GetMapping("/get-all")
    fun getAllCreatedGames(): List<GameDto> {
        return gameService.getAllCreatedGames().map { GameDto.fromDomain(it) }
    }

    @PostMapping("/join-game")
    fun joinGame(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody joinGameRequest: JoinGameRequest
    ): JoinGameResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.joinGame(user, joinGameRequest)
    }

    @GetMapping("/get-active")
    fun getUserActiveGame(@RequestHeader("Authorization") authorization: String): GameDto? {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getUserActiveGame(user)?.let { GameDto.fromDomain(it) }
    }

    @PostMapping("/make-move")
    fun makeMove(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody gameMakeMoveRequest: GameMakeMoveRequest
    ): MakeMoveResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.makeMove(user, gameMakeMoveRequest)
    }


    @GetMapping("/get-in-progress")
    fun getUserActiveGameAndReturnMoves(@RequestHeader("Authorization") authorization: String): MakeMoveResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getUserActiveGameAndReturnMoves(user)
    }

    @PutMapping("/resign")
    fun resign(
        @RequestHeader("Authorization") authorization: String
    ): GameDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return GameDto.fromDomain(gameService.resign(user))
    }

    @PostMapping("/draw-offers")
    fun newOffer(
        @RequestHeader("Authorization") authorization: String
    ): Long? {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.createOffer(user)
    }

    @PutMapping("/draw-offers/response")
    fun responseOffer(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody gameDrawOfferRequest: GameDrawOfferRequest
    ): Long {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.responseOffer(user, gameDrawOfferRequest)
    }

    @GetMapping("/draw-offers")
    fun getDrawOffer(@RequestHeader("Authorization") authorization: String): DrawOffersDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return DrawOffersDto.fromDomain(gameService.getDrawOffer(user))
    }

    @GetMapping("/default-position-editor")
    fun getDefaultPiecesPosition(
        @RequestHeader("Authorization") authorization: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getDefaultPiecesToPositionEditor(user)
    }

    @GetMapping("/position-editor")
    fun getPositionEditorPieces(
        @RequestHeader("Authorization") authorization: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return gameService.getPositionEditorPieces(user)
    }

    @PutMapping("/remove-piece")
    fun removePieceFromPositionEditor(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam pieceId: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)
        return gameService.removePieceFromPositionEditor(pieceId, user)
    }

    @PutMapping("/new-position")
    fun changePositionOfPiece(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody changesIds: ChangePositionOfPieceInPositionEditor
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)
        return gameService.changePositionOfPiece(changesIds, user)
    }
}