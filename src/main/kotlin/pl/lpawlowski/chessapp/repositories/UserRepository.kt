package pl.lpawlowski.chessapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import pl.lpawlowski.chessapp.entities.User
import java.util.*

interface UsersRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): Optional<User>
    fun findByLoginAndPassword(login: String, password: String): Optional<User>
    fun existsByLogin(login: String): Boolean
    fun findByActiveToken(activeToken: String): Optional<User>
}