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
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.ChangePasswordRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.model.user.UserLogInRequest
import pl.lpawlowski.chessapp.repositories.UsersRepository

class UserServiceTests : BasicIntegrationTest() {
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
        val userDto = UserDto(
            login = testUserLogin,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val newPassword = "newpassword123"
        val passwordRequest = ChangePasswordRequest(userDto.password!!, newPassword)
        val user = userRepository.findAll()
        val userDtoAfterUpdate = userService.updateUserPassword(user[0], passwordRequest)

        assert(passwordEncoder.matches(newPassword, userDtoAfterUpdate.password))
    }

    @Test
    fun testGetUserByLogin() {
        val wrongLogin = "Daniel"
        val secondUserLogin = "sebastian"
        val thirdUserLogin = "maciej"

        insertUser(testUserLogin)
        insertUser(secondUserLogin)
        insertUser(thirdUserLogin)

        assertThat(userService.getUserByLogin(secondUserLogin).login).isEqualTo(secondUserLogin)
        assertThat(userService.getUserByLogin(testUserLogin).login).isEqualTo(testUserLogin)
        assertThrows<RuntimeException> { userService.getUserByLogin(wrongLogin) }
    }

    @Test
    fun testLogIn() {
        val userDto = UserDto(
            login = testUserLogin,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val wrongLogin = "krzysztof"
        val logInRequest = UserLogInRequest(testUserLogin, userDto.password!!)
        val wrongLogInRequest = UserLogInRequest(wrongLogin, userDto.password!!)

        assertThrows<WrongCredentialsException> { userService.logIn(wrongLogInRequest) }
        assertThat(userService.logIn(logInRequest).email).isEqualTo(testsUserEmail)
    }

    @Test
    fun testFindUserByAuthorizationToken() {
        val secondUserLogin = "sebastian"
        val userDto = UserDto(
            login = testUserLogin,
            password = "jan12345",
            email = testsUserEmail
        )
        val secondUserDto = UserDto(
            login = secondUserLogin,
            password = "sebastian12",
            email = "sebastian@onet.pl"
        )

        userService.saveUser(userDto)
        userService.saveUser(secondUserDto)
        insertUser()
        insertUser()
        insertUser()
        userService.logIn(UserLogInRequest(userDto.login, userDto.password!!))
        userService.logIn(UserLogInRequest(secondUserDto.login, secondUserDto.password!!))

        val loggedUser = userService.findUserByLogin(testUserLogin)
        val correctToken = loggedUser.activeToken!!

        assertDoesNotThrow { userService.findUserByAuthorizationToken(correctToken) }
        assertThat(userService.findUserByAuthorizationToken(correctToken).login).isEqualTo(testUserLogin)
    }
}