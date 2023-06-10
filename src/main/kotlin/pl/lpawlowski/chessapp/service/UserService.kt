package pl.lpawlowski.chessapp.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.ChangePasswordRequest
import pl.lpawlowski.chessapp.model.user.PlayerInfoDto
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.model.user.UserLogInRequest
import pl.lpawlowski.chessapp.repositories.UsersRepository
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val usersRepository: UsersRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun saveUser(userDto: UserDto): Long {
        requireNotNull(userDto.email) { "Email must not be null" }
        requireNotNull(userDto.password) { "Password must not be null" }

        when {
            usersRepository.existsByLogin(userDto.login) -> throw UserExistsException("User ${userDto.login} already exists")
            else -> {
                val user: User = User().apply {
                    login = userDto.login
                    email = userDto.email
                    password = userDto.password
                }

                val encodedPassword = passwordEncoder.encode(user.password)

                user.password = encodedPassword

                return usersRepository.save(user).id!!
            }
        }
    }


    fun getUserByLogin(login: String): User =
        usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") }

    @Transactional
    fun logIn(userLogInRequest: UserLogInRequest): User {
        val user = findUserByLogin(userLogInRequest.login)

        return when {
            passwordEncoder.matches(userLogInRequest.password, user.password) -> {
                val token = "${LocalDateTime.now()}${user.password}${user.login}${Random().nextLong()}"
                val localDateTimePlusHour = LocalDateTime.now().plusHours(1)

                user.activeToken = token
                user.validUtil = localDateTimePlusHour
                user
            }

            else -> throw WrongCredentialsException("Incorrect login or password!")
        }
    }

    @Transactional
    fun updateUserLogin(login: String, newLogin: String): User {
        check(!usersRepository.existsByLogin(newLogin)) { "User $newLogin already exists" }

        val user = usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") }

        user.login = newLogin

        return user
    }

    @Transactional
    fun updateUserEmail(login: String, newEmail: String): User {
        val user = usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") }

        user.email = newEmail

        return user
    }

    @Transactional
    fun updateUserPassword(user: User, changePasswordRequest: ChangePasswordRequest): User {
        if (!passwordEncoder.matches(changePasswordRequest.oldPassword, user.password)) {
            throw WrongCredentialsException("Incorrect old password!")
        }

        val encodedPassword = passwordEncoder.encode(changePasswordRequest.password)

        user.password = encodedPassword

        return user
    }

    fun getAllUsers(): List<User> = usersRepository.findAll()
    fun findUserByLogin(login: String): User =
        usersRepository.findByLogin(login).orElseThrow { WrongCredentialsException("Incorrect login or password") }
    fun findUserByAuthorizationToken(activeToken: String): User {
        return usersRepository.findByActiveToken(activeToken)
            .filter { it.validUtil?.isAfter(LocalDateTime.now()) ?: false }
            .orElseThrow { WrongCredentialsException("No active Token found") }
    }

    fun getAllPlayersInfo(): List<PlayerInfoDto> = usersRepository.findAll().map { PlayerInfoDto.fromDomain(it) }
}