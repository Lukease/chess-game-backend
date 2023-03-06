package pl.lpawlowski.chessapp.model.user

import java.time.LocalDateTime

data class UserActiveToken(
    val activeToken: String,
    val validUtil: LocalDateTime
)