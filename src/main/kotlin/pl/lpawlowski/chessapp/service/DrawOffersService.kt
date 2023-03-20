package pl.lpawlowski.chessapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.DrawOffersNotFoundException
import pl.lpawlowski.chessapp.game.DrawOffersStatus
import pl.lpawlowski.chessapp.game.GameResult
import pl.lpawlowski.chessapp.game.GameStatus
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

        return if (gameOffer.isPresent) {
            val drawOffers: DrawOffers = DrawOffers().apply {
                game = gameOffer.get()
                status = DrawOffersStatus.OFFERED.name
                playerOffered = user
                playerResponding = if (game.whitePlayer == user) {
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
    fun responseOffer(user: User, gameDrawOfferRequest: GameDrawOfferRequest) {
        val drawOffer = drawOffersRepository.findByUserAndStatus(user, DrawOffersStatus.OFFERED.name)
            .orElseThrow { DrawOffersNotFoundException("Offer not found!") }


        if (gameDrawOfferRequest.playerResponse) {
            drawOffer.game.gameStatus = GameStatus.FINISHED.name
            drawOffer.game.result = GameResult.DRAW.name
            drawOffer.status = DrawOffersStatus.ACCEPTED.name

        } else {
            drawOffer.status = DrawOffersStatus.REJECTED.name
        }
    }
}