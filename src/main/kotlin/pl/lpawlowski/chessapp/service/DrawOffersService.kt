package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.offers.DrawOffersDto
import pl.lpawlowski.chessapp.model.offers.GameDrawOfferRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository

@Service
class DrawOffersService(
    private val gamesRepository: GamesRepository,
    private val drawOffersRepository: DrawOffersRepository
) {
    @Transactional
    fun createOffer(user: User): Long? {
        val gameOffer = gamesRepository.findByUserAndStatus(user, GameStatus.IN_PROGRESS.name)
            .orElseThrow { NotFound("No active game!!") }

        return if (gameOffer != null) {
            val drawOffers: DrawOffers = DrawOffers().apply {
                game = gameOffer
                status = DrawOffersStatus.OFFERED.name
                playerOffered = user
                playerResponding = if (game.whitePlayer?.login == user.login) {
                    game.blackPlayer!!
                } else {
                    game.whitePlayer!!
                }
            }

            val save: DrawOffers = drawOffersRepository.save(drawOffers)

            return save.id!!
        } else {
            null
        }
    }

    @Transactional
    fun responseOffer(user: User, gameDrawOfferRequest: GameDrawOfferRequest): Long {
        val drawOffer = drawOffersRepository.findById(gameDrawOfferRequest.gameOfferId)
            .orElseThrow { NotFound("Draw offer not found!") }

        if (drawOffer.playerResponding.login == user.login) {
            if (gameDrawOfferRequest.playerResponse) {
                drawOffer.game.gameStatus = GameStatus.FINISHED.name
                drawOffer.game.result = GameResult.DRAW.name
                drawOffer.status = DrawOffersStatus.ACCEPTED.name
            } else {
                drawOffer.status = DrawOffersStatus.REJECTED.name
            }
        }

        return drawOffer.id!!
    }

    fun getDrawOffer(user: User): DrawOffersDto {
        val drawOffer = drawOffersRepository.findByUserAndStatus(user, DrawOffersStatus.OFFERED.name)
            .orElseThrow { NotFound("Draw offer not found!") }

        return DrawOffersDto.fromDomain(drawOffer)
    }
}