package pl.lpawlowski.chessapp.model.user

import pl.lpawlowski.chessapp.entities.User
import java.time.LocalDateTime

data class UserDto(
    val id: Long? = null,
    val login: String,
    val password: String,
    val email: String,
    val activeToken: String? = null,
    val validUtil: LocalDateTime? = null
) {
    companion object {
        fun fromDomain(user: User): UserDto {
            return UserDto(
                id = user.id,
                login = user.login,
                password = user.password,
                email = user.email,
                activeToken = user.activeToken,
                validUtil = user.validUtil
            )
        }
    }
}