package pl.lpawlowski.chessapp.model.user

data class UserLogInRequest(
    val login: String,
    val password: String
)