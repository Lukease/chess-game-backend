package pl.lpawlowski.chessapp.model.offers

data class GameDrawOfferRequest (
    val gameOfferId: Long,
    val playerResponse: Boolean
)