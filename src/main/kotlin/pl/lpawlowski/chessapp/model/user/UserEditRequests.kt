package pl.lpawlowski.chessapp.model.user

data class ChangeLoginRequest(
    val login: String
)

data class ChangeEmailRequest(
    val email: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val password: String
)