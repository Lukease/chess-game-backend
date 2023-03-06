package pl.lpawlowski.chessapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import java.util.*

interface GamesRepository : JpaRepository<Game, Long> {
    fun findById(id: Double): Optional<Game>
    fun findByUser(user: User): Optional<Game>
}