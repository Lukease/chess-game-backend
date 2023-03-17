package pl.lpawlowski.chessapp.model.offers

import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.Game

class DrawOffersDto(
    var id: Long? = null,
    var game: Game,
    val status: String
) {
    companion object {
        fun fromDomain(drawOffers: DrawOffers): DrawOffersDto {
            return DrawOffersDto(
                id = drawOffers.id,
                game = drawOffers.game,
                status = drawOffers.status
            )
        }
    }
}