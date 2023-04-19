package pl.lpawlowski.chessapp.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.repositories.UsersRepository
import pl.lpawlowski.chessapp.web.pieces.*

@SpringBootTest
class BasicIntegrationTest {
    @Autowired
    lateinit var userRepository: UsersRepository

    val testUserLogin = "Kuba"
    val testsUserEmail = "Kuba123@gmail.com"


    fun insertUser(login: String? = null) {
        userRepository.save(User().also {
            it.login = login ?: RandomStringUtils.randomAlphabetic(7)
            it.email = "${RandomStringUtils.randomAlphabetic(7)}@gmail.com"
            it.password = RandomStringUtils.randomAlphanumeric(7)
        })
    }
}