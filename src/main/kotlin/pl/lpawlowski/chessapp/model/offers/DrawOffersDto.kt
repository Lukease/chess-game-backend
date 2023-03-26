package pl.lpawlowski.chessapp.model.offers

import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.Game

class DrawOffersDto(
    var id: Long? = null,
    val status: String
) {
    companion object {
        fun fromDomain(drawOffers: DrawOffers): DrawOffersDto {
            return DrawOffersDto(
                id = drawOffers.id,
                status = drawOffers.status
            )
        }
    }
}