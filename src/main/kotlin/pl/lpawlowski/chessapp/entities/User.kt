package pl.lpawlowski.chessapp.entities

import java.time.LocalDateTime
import javax.persistence.*

@Table(name = "users")
@Entity
class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null

    lateinit var login: String
    lateinit var password: String
    lateinit var email: String
    var activeToken: String? = null
    var validUtil: LocalDateTime? = null
    @OneToMany(mappedBy = "whitePlayer")
    var gamesAsWhite: List<Game> = listOf()
    @OneToMany(mappedBy = "blackPlayer")
    var gamesAsBlack: List<Game> = listOf()
    @OneToMany(mappedBy = "playerOffered")
    var offeredDraw: List<DrawOffers> = listOf()
    @OneToMany(mappedBy = "playerResponding")
    var respondingDraw: List<DrawOffers> = listOf()
}