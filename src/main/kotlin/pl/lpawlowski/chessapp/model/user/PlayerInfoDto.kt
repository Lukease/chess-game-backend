package pl.lpawlowski.chessapp.model.user

import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.game.GameResult

class PlayerInfoDto(
    val id: Long? = null,
    val login: String,
    val email: String,
    val gamesAsWhite: Int,
    val gamesAsBlack: Int,
    val allGames: Int,
    val wins: Int,
    val winRatio: Long? = null,
    val losses: Int? = null,
    val draws: Int
) {
    companion object {
        fun fromDomain(user: User): PlayerInfoDto {
            val gamesAsWhite = user.gamesAsWhite.size
            val gamesAsBlack = user.gamesAsBlack.size
            val allGames = gamesAsWhite + gamesAsBlack
            val wins = user.gamesAsWhite.count { it.result == GameResult.WHITE.name } + user.gamesAsBlack.count { it.result == GameResult.BLACK.name }
            val losses = user.gamesAsWhite.count { it.result == GameResult.WHITE.name } + user.gamesAsBlack.count { it.result == GameResult.BLACK.name }
            val winRatio = if (allGames > 0) (wins.toDouble() / allGames.toDouble() * 100).toLong() else null
            val draws = user.gamesAsWhite.count { it.result == GameResult.DRAW.name } + user.gamesAsBlack.count { it.result == GameResult.DRAW.name }

            return PlayerInfoDto(
                id = user.id,
                login = user.login,
                email = user.email,
                gamesAsWhite = gamesAsWhite,
                gamesAsBlack = gamesAsBlack,
                allGames = allGames,
                wins = wins,
                winRatio = winRatio,
                losses = losses,
                draws = draws
            )
        }
    }
}