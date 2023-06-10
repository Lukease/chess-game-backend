package pl.lpawlowski.chessapp.entities

import pl.lpawlowski.chessapp.game.DrawOffersStatus
import javax.persistence.*

@Table(name = "draw_offers")
@Entity
class DrawOffers {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null
    var status: String = DrawOffersStatus.OFFERED.name

    @ManyToOne
    @JoinColumn(name = "game_id")
    lateinit var game: Game

    @ManyToOne
    @JoinColumn(name = "player_offered_id")
    lateinit var playerOffered: User

    @ManyToOne
    @JoinColumn(name = "player_responding_id")
    lateinit var playerResponding: User
}