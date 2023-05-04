package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.ChangePasswordRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.model.user.UserLogInRequest

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
            login = testUserLogin1,
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
        insertUser(testUserLogin1)

        val userDto = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )

        assertThrows<UserExistsException> { userService.saveUser(userDto) }
    }

    @Test
    fun testEditUserEmail() {
        insertUser(login = testUserLogin1)

        val newEmail = "jurek@gmail.com"

        userService.updateUserEmail(testUserLogin1, newEmail)

        val allUsers = userRepository.findAll()

        assertThat(allUsers.size).isEqualTo(1)
        assertThat(allUsers[0].email).isEqualTo(newEmail)
    }

    @Test
    fun testEditUserLogin() {
        insertUser(login = testUserLogin1)

        val newLogin = "123456!A"

        userService.updateUserLogin(testUserLogin1, newLogin)

        val allUsers = userRepository.findAll()

        assertThat(allUsers[0].login).isEqualTo(newLogin)
    }

    @Test
    fun testEditUserPassword() {
        val userDto = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)
        val userBeforeChanges = userRepository.findByLogin(testUserLogin1)
        val newPassword = "newpassword123"
        val passwordRequest = ChangePasswordRequest(userDto.password!!, newPassword)
        val user = userRepository.findByLogin(testUserLogin1)
        val userAfterUpdate = userService.updateUserPassword(user.get(), passwordRequest)

        assert(passwordEncoder.matches(newPassword, userAfterUpdate.password))
        assertThat(userBeforeChanges.get().password).isNotEqualTo(userAfterUpdate.password)
    }

    @Test
    fun testGetUserByLogin() {
        val wrongLogin = "Daniel"
        val secondUserLogin = "sebastian"
        val thirdUserLogin = "maciej"

        insertUser(testUserLogin1)
        insertUser(secondUserLogin)
        insertUser(thirdUserLogin)

        assertThat(userService.getUserByLogin(secondUserLogin).login).isEqualTo(secondUserLogin)
        assertThat(userService.getUserByLogin(testUserLogin1).login).isEqualTo(testUserLogin1)
        assertThrows<RuntimeException> { userService.getUserByLogin(wrongLogin) }
    }

    @Test
    fun testLogIn() {
        val userDto = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val wrongLogin = "krzysztof"
        val logInRequest = UserLogInRequest(testUserLogin1, userDto.password!!)
        val wrongLogInRequest = UserLogInRequest(wrongLogin, userDto.password!!)

        assertThrows<WrongCredentialsException> { userService.logIn(wrongLogInRequest) }
        assertThat(userService.logIn(logInRequest).email).isEqualTo(testsUserEmail)
    }

    @Test
    fun testFindUserByAuthorizationToken() {
        val userDto1 = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )
        val userDto2 = UserDto(
            login = testUserLogin2,
            password = "jan12345672",
            email = testsUserEmail
        )

        userService.saveUser(userDto1)
        userService.saveUser(userDto2)
        insertUser()
        insertUser()
        userService.logIn(UserLogInRequest(userDto1.login, userDto1.password!!))
        userService.logIn(UserLogInRequest(userDto2.login, userDto2.password!!))

        val loggedUser = userService.findUserByLogin(userDto1.login)
        val correctToken = loggedUser.activeToken!!

        val searchedUser = userService.findUserByAuthorizationToken(correctToken)

        assertThat(searchedUser.login).isEqualTo(testUserLogin1)
        assertThat(searchedUser.email).isEqualTo(testsUserEmail)
        assertThat(searchedUser.activeToken).isEqualTo(searchedUser.activeToken)
        assertThat(searchedUser.validUtil).isEqualTo(searchedUser.validUtil)
    }

    @Test
    fun testNoActiveToken() {
        val token = "wrongToken"
        val userDto = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)
        insertUser()
        insertUser()

        assertThrows<WrongCredentialsException> { userService.findUserByAuthorizationToken(token) }
    }

    @Test
    fun testGetAllUsersStatistics() {
        val secondUserLogin = "sebastian"
        val userDto = UserDto(
            login = testUserLogin1,
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

        val allUserStats = userService.getAllPlayersInfo()

        assertThat(allUserStats.size).isEqualTo(5)
    }

    @Test
    fun testTryToChangePasswordWithPassWrongCurrent() {
        val password = "jan12345"
        val wrongPassword = "12345jan"
        val userDto = UserDto(
            login = testUserLogin1,
            password = password,
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val newPassword = "newpassword123"
        val passwordRequest = ChangePasswordRequest(wrongPassword, newPassword)
        val user = userRepository.findAll()

        assertThrows<WrongCredentialsException> { userService.updateUserPassword(user[0], passwordRequest) }
    }

    @Test
    fun testLogInWithWrongData() {
        val userDto = UserDto(
            login = testUserLogin1,
            password = "jan12345",
            email = testsUserEmail
        )

        userService.saveUser(userDto)

        val wrongPassword = "krzysztof"
        val wrongLogInRequest = UserLogInRequest(testUserLogin1, wrongPassword)

        assertThrows<WrongCredentialsException> { userService.logIn(wrongLogInRequest) }
    }
}