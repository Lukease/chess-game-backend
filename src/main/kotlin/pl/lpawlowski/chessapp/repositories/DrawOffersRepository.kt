package pl.lpawlowski.chessapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pl.lpawlowski.chessapp.entities.DrawOffers
import pl.lpawlowski.chessapp.entities.User
import java.util.*

interface DrawOffersRepository: JpaRepository<DrawOffers, Long>  {
    @Query("Select d from DrawOffers d where d.playerResponding = ?1 and d.status = ?2 ")
    fun findByUserAndStatus(user: User, status: String): Optional<DrawOffers>
}