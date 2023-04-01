package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.ChangePasswordRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.model.user.UserLogInRequest
import pl.lpawlowski.chessapp.repositories.UsersRepository

class UserServiceTests: BasicIntegrationTest() {
    @Autowired
    lateinit var userService: UserService


    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @AfterEach
    fun cleanUpDatabase() {
        userRepository.deleteAll()
    }

    @Test
    fun testCreateUser() {
        val userDto = UserDto(
            login = testUserLogin,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val allUsers = userRepository.findAll()

        assertThat(allUsers.size).isEqualTo(1)
        assertThat(allUsers[0].login).isEqualTo(userDto.login)
        assertThat(allUsers[0].email).isEqualTo(userDto.email)
    }

    @Test
    fun testUserExists() {
        insertUser(testUserLogin)

        val userDto = UserDto(
            login = testUserLogin,
            password = "jan12345",
            email = testsUserEmail
        )

        assertThrows<UserExistsException> { userService.saveUser(userDto) }
    }

    @Test
    fun testEditUserEmail() {
        insertUser(login = testUserLogin)

        val newEmail = "jurek@gmail.com"

        userService.updateUserEmail(testUserLogin, newEmail)

        val allUsers = userRepository.findAll()

        assertThat(allUsers.size).isEqualTo(1)
        assertThat(allUsers[0].email).isEqualTo(newEmail)
    }

    @Test
    fun testEditUserLogin() {
        insertUser(login = testUserLogin)

        val newLogin = "123456!A"

        userService.updateUserLogin(testUserLogin, newLogin)

        val allUsers = userRepository.findAll()

        assertThat(allUsers[0].login).isEqualTo(newLogin)
    }

    @Test
    fun testEditUserPassword() {
        val userLogin = "jan"
        val userDto = UserDto(
            login = userLogin,
            password = "jan12345",
            email = "jan@onet.pl"
        )

        userService.saveUser(userDto)

        val oldPassword = "jan12345"
        val newPassword = "123456!A"
//        val passwordRequest = ChangePasswordRequest(oldPassword, newPassword)
//        val user = userService.updateUserPassword(userDto.login, passwordRequest)
//
//        val user: User = userRepository.findByLogin("jan")

//        assert(passwordEncoder.matches(newPassword, user.password))
    }

    @Test
    fun testGetUserByLogin() {
        val firstUserDto = UserDto(
            login = "jan",
            password = "jan12345",
            email = "jan@onet.pl"
        )
        val secondUserDto = UserDto(
            login = "sebastian",
            password = "sebastian12",
            email = "sebastian@onet.pl"
        )
        val thirdUserDto = UserDto(
            login = "kuba",
            password = "kuba12",
            email = "kuba@onet.pl"
        )
        val wrongLogin = "Daniel"

        userService.saveUser(firstUserDto)
        userService.saveUser(secondUserDto)
        userService.saveUser(thirdUserDto)

        val allUsers = userRepository.findAll()
        val searchedUser = allUsers[1]
        val getUserByLoginTest = userService.getUserByLogin(secondUserDto.login)

        assertThat(getUserByLoginTest.login).isEqualTo(searchedUser.login)
        assertThrows<RuntimeException> { userService.getUserByLogin(wrongLogin) }
    }

    @Test
    fun testLogIn() {
        val userDto = UserDto(
            login = "jan",
            password = "jan12345",
            email = "jan@onet.pl"
        )

        userService.saveUser(userDto)

        val wrongLogin = "krzysztof"
        val logInRequest = UserLogInRequest(userDto.login, userDto.password!!)
        val wrongLogInRequest = UserLogInRequest(wrongLogin, userDto.password!!)

        assertThrows<WrongCredentialsException> { userService.logIn(wrongLogInRequest) }
        assertDoesNotThrow { userService.logIn(logInRequest) }
    }

    @Test
    fun testFindUserByAuthorizationToken() {
        val userDto = UserDto(
            login = "jan",
            password = "jan12345",
            email = "jan@onet.pl"
        )
        val secondUserDto = UserDto(
            login = "sebastian",
            password = "sebastian12",
            email = "sebastian@onet.pl"
        )

        userService.saveUser(userDto)
        userService.saveUser(secondUserDto)

        val logInRequest = UserLogInRequest(userDto.login, userDto.password!!)

        userService.logIn(logInRequest)

        val allUsers = userRepository.findAll()
        val loggedUser = allUsers[0]
        val correctToken = loggedUser.activeToken!!

        assertDoesNotThrow { userService.findUserByAuthorizationToken(correctToken) }
        assertThat(loggedUser.activeToken).isNotNull
        assertThat(loggedUser.validUtil).isNotNull
    }
}