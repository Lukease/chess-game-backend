package pl.lpawlowski.chessapp.controller

import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.offers.DrawOffersDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.service.DrawOffersService
import pl.lpawlowski.chessapp.service.UserService

@CrossOrigin(origins = ["http://localhost:3000/"])
@RestController
@RequestMapping(value = ["/draw-offers"])
class DrawOffersController(
    private val drawOffersService: DrawOffersService,
    private val userService: UserService,
) {
    @PostMapping
    fun newOffer(
        @RequestHeader("Authorization") authorization: String
    ): Long? {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return drawOffersService.createOffer(user)
    }

    @PutMapping("/response")
    fun responseOffer(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody gameDrawOfferRequest: GameDrawOfferRequest
    ): Long {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return drawOffersService.responseOffer(user, gameDrawOfferRequest)
    }

    @GetMapping
    fun getDrawOffer(@RequestHeader("Authorization") authorization: String): DrawOffersDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return drawOffersService.getDrawOffer(user)
    }
}