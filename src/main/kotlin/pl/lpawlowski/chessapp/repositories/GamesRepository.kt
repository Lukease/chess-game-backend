package pl.lpawlowski.chessapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import java.util.*

interface GamesRepository : JpaRepository<Game, Long> {
    @Query("Select g from Game g where g.whitePlayer = ?1 or g.blackPlayer = ?1 and g.gameStatus = ?2")
    fun findByUserAndStatus(user: User, status: String): Optional<Game>

    @Query("Select g from Game g where g.gameStatus = ?1")
    fun findGamesByStatus( status: String): List<Game>
}